package niwer.photon.util.session;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.reflect.TypeToken;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.util.GsonUtils;

public final class AdminSessionManager {

    private static final File SESSION_FILE = new File(Directories.BASE_DIR, "admin_sessions.json");
    private static final Type SESSION_MAP_TYPE = new TypeToken<Map<String, SessionSnapshot>>() {}.getType();
    private static final Map<String, SessionSnapshot> SESSIONS = new ConcurrentHashMap<>();

    private AdminSessionManager() {}

    static {
        load();
    }
    
    public static synchronized void load() {
        SESSIONS.clear();

        if (!SESSION_FILE.exists()) return;

        try (FileReader reader = new FileReader(SESSION_FILE)) {
            final Map<String, SessionSnapshot> loadedSessions = GsonUtils.GSON.fromJson(reader, SESSION_MAP_TYPE);
            if (loadedSessions != null) {
                SESSIONS.putAll(loadedSessions);
            }
        } catch (Exception ignored) {}
    }

    private static synchronized void save() {
        if (!Directories.BASE_DIR.exists()) Directories.BASE_DIR.mkdirs();

        try (FileWriter writer = new FileWriter(SESSION_FILE)) {
            GsonUtils.GSON.toJson(SESSIONS, SESSION_MAP_TYPE, writer);
        } catch (Exception ignored) {}
    }

    public static AuthSession login(String email, String password) {
        if (email == null || password == null) return null;

        final ObjectUserAccount account = PlayerAccountTable.getAccountByEmail(email);
        if (account == null || account.password() == null || !account.isAdministrator()) return null;
        if (!PlayerAccountTable.passwordMatches(account.password(), password)) return null;

        if (!PlayerAccountTable.isArgon2Password(account.password())) {
            PlayerAccountTable.setPassword(account.getUuid(), password);
        }

        final String token = UUID.randomUUID().toString().replace("-", "");
        final String csrf = UUID.randomUUID().toString().replace("-", "");
        SESSIONS.put(token, new SessionSnapshot(snapshot(account), System.currentTimeMillis(), csrf));
        save();
        return new AuthSession(token, account);
    }

    public static String getCsrfForToken(String token) {
        if (token == null || token.isBlank()) return null;
        final SessionSnapshot session = SESSIONS.get(token);
        if (session == null) return null;
        return session.csrf();
    }

    public static ObjectUserAccount requireAdministrator(Context handler) {
        final ObjectUserAccount account = accountFromRequest(handler);
        if (account == null) {
            handler.status(401).result("Unauthorized");
            return null;
        }

        if (!account.isAdministrator()) {
            handler.status(403).result("Administrator access required");
            return null;
        }

        return account;
    }

    public static ObjectUserAccount accountFromRequest(Context handler) {
        final String token = extractToken(handler);
        if (token == null || token.isBlank()) {
            final ObjectUserAccount userAccount = UserSessionManager.accountFromRequest(handler);
            return userAccount != null && userAccount.isAdministrator() ? userAccount : null;
        }

        final SessionSnapshot session = SESSIONS.get(token);
        if (session == null) {
            final ObjectUserAccount userAccount = UserSessionManager.accountFromRequest(handler);
            return userAccount != null && userAccount.isAdministrator() ? userAccount : null;
        }

        final AccountSnapshot snapshot = session.account();
        if (snapshot == null || snapshot.uuid() == null || snapshot.uuid().isBlank()) return null;

        final ObjectUserAccount account = PlayerAccountTable.getAccountByUUID(snapshot.uuid());
        if (account == null) {
            return ObjectUserAccount.fromSnapshot(
                snapshot.username(),
                snapshot.email(),
                snapshot.uuid(),
                snapshot.discordID(),
                snapshot.discordAuthCode(),
                snapshot.administrator(),
                snapshot.serverCreator()
            );
        }

        return account;
    }

    public static void logout(String token) {
        if (token != null && !token.isBlank()) {
            SESSIONS.remove(token);
            save();
        }
    }

    private static String extractToken(Context handler) {
        // Prefer cookie-based sessions (HttpOnly) to avoid exposing tokens to JS/localStorage
        try {
            final String cookieToken = handler.cookie("photon_admin");
            if (cookieToken != null && !cookieToken.isBlank()) return cookieToken.trim();
        } catch (Exception ignored) {}

        // Fallback to bearer Authorization header only. Do NOT accept tokens from query/form parameters.
        final String authorization = handler.header("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }

        return null;
    }

    public static boolean validateCsrf(Context handler) {
        try {
            final String token = extractToken(handler);
            if (token == null || token.isBlank()) return false;

            final SessionSnapshot session = SESSIONS.get(token);
            if (session == null) return false;

            final String expected = session.csrf();
            if (expected == null || expected.isBlank()) return false;

            final String header = handler.header("X-CSRF-Token");
            if (header != null && header.equals(expected)) return true;

            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static AccountSnapshot snapshot(ObjectUserAccount account) {
        return new AccountSnapshot(
            account.getUsername(),
            account.getEmail(),
            account.getUuid(),
            account.getDiscordID(),
            account.getDiscordAuthCode(),
            account.isAdministrator(),
            account.isServerCreator()
        );
    }
}
package niwer.photon.web;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.javalin.http.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;

public final class AdminSessionManager {

    private static final File SESSION_FILE = new File(Directories.BASE_DIR, "admin_sessions.json");
    private static final Gson GSON = Directories.GSON;
    private static final Type SESSION_MAP_TYPE = new TypeToken<Map<String, Session>>() {}.getType();
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private AdminSessionManager() {}

    static {
        load();
    }

    public record AuthSession(String token, ObjectPlayerAccount account) {}

    private record AccountSnapshot(String username, String email, String uuid, String discordID, String discordAuthCode, boolean administrator, boolean serverCreator) {}
    
    private record Session(AccountSnapshot account, long createdAt, String csrf) {}

    public static synchronized void load() {
        SESSIONS.clear();

        if (!SESSION_FILE.exists()) return;

        try (FileReader reader = new FileReader(SESSION_FILE)) {
            final Map<String, Session> loadedSessions = GSON.fromJson(reader, SESSION_MAP_TYPE);
            if (loadedSessions != null) {
                SESSIONS.putAll(loadedSessions);
            }
        } catch (Exception ignored) {}
    }

    private static synchronized void save() {
        if (!Directories.BASE_DIR.exists()) Directories.BASE_DIR.mkdirs();

        try (FileWriter writer = new FileWriter(SESSION_FILE)) {
            GSON.toJson(SESSIONS, SESSION_MAP_TYPE, writer);
        } catch (Exception ignored) {}
    }

    public static AuthSession login(String email, String password) {
        if (email == null || password == null) return null;

        final ObjectPlayerAccount account = PlayerAccountTable.getAccountByEmail(email);
        if (account == null || account.password() == null || !account.password().equals(password) || !account.isAdministrator()) return null;

        final String token = UUID.randomUUID().toString().replace("-", "");
        final String csrf = UUID.randomUUID().toString().replace("-", "");
        SESSIONS.put(token, new Session(snapshot(account), System.currentTimeMillis(), csrf));
        save();
        return new AuthSession(token, account);
    }

    public static String getCsrfForToken(String token) {
        if (token == null || token.isBlank()) return null;
        final Session session = SESSIONS.get(token);
        if (session == null) return null;
        return session.csrf();
    }

    public static ObjectPlayerAccount requireAdministrator(Context handler) {
        final ObjectPlayerAccount account = accountFromRequest(handler);
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

    public static ObjectPlayerAccount accountFromRequest(Context handler) {
        final String token = extractToken(handler);
        if (token == null || token.isBlank()) {
            final ObjectPlayerAccount userAccount = UserSessionManager.accountFromRequest(handler);
            return userAccount != null && userAccount.isAdministrator() ? userAccount : null;
        }

        final Session session = SESSIONS.get(token);
        if (session == null) {
            final ObjectPlayerAccount userAccount = UserSessionManager.accountFromRequest(handler);
            return userAccount != null && userAccount.isAdministrator() ? userAccount : null;
        }

        final AccountSnapshot snapshot = session.account();
        if (snapshot == null || snapshot.uuid() == null || snapshot.uuid().isBlank()) return null;

        final ObjectPlayerAccount account = PlayerAccountTable.getAccountByUUID(snapshot.uuid());
        if (account == null) {
            return ObjectPlayerAccount.fromSnapshot(
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

            final Session session = SESSIONS.get(token);
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

    private static AccountSnapshot snapshot(ObjectPlayerAccount account) {
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
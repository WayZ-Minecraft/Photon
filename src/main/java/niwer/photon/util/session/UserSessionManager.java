package niwer.photon.util.session;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;

public final class UserSessionManager {

    private static final File SESSION_FILE = new File(Directories.BASE_DIR, "user_sessions.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SESSION_MAP_TYPE = new TypeToken<Map<String, SessionSnapshot>>() {}.getType();
    private static final Map<String, SessionSnapshot> SESSIONS = new ConcurrentHashMap<>();

    private UserSessionManager() {}

    public static void load() {
        SESSIONS.clear();
        if (!SESSION_FILE.exists()) return;

        try (FileReader reader = new FileReader(SESSION_FILE)) {
            final Map<String, SessionSnapshot> loadedSessions = GSON.fromJson(reader, SESSION_MAP_TYPE);
            if (loadedSessions != null) {
                SESSIONS.putAll(loadedSessions);
            }
        } catch (Exception ignored) {}
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(SESSION_FILE)) {
            GSON.toJson(SESSIONS, SESSION_MAP_TYPE, writer);
        } catch (Exception ignored) {}
    }

    public static AuthSession login(String email, String password) {
        final ObjectUserAccount account = PlayerAccountTable.getAccountByEmail(email);
                if (account == null || password == null || !PlayerAccountTable.passwordMatches(account.password(), password)) return null;

		if (!PlayerAccountTable.isArgon2Password(account.password())) {
			PlayerAccountTable.setPassword(account.getUuid(), password);
		}

        final String token = UUID.randomUUID().toString().replace("-", "");
        SESSIONS.put(token, new SessionSnapshot(snapshot(account), System.currentTimeMillis()));
        save();
        return new AuthSession(token, account);
    }

    public static AuthSession createSessionForAccount(ObjectUserAccount account) {
        if (account == null) return null;
        final String token = UUID.randomUUID().toString().replace("-", "");
        SESSIONS.put(token, new SessionSnapshot(snapshot(account), System.currentTimeMillis()));
        save();
        return new AuthSession(token, account);
    }

    public static ObjectUserAccount requireAccount(Context handler) {
        final ObjectUserAccount account = accountFromRequest(handler);
        if (account == null) {
            handler.status(401).result("Unauthorized");
        }
        return account;
    }

    public static ObjectUserAccount accountFromRequest(Context handler) {
        final String token = extractToken(handler);
        if (token == null || token.isBlank()) return null;

        final SessionSnapshot session = SESSIONS.get(token);
        if (session == null) return null;

        final AccountSnapshot snapshot = session.account();
        if (snapshot == null || snapshot.uuid() == null || snapshot.uuid().isBlank()) return null;

        final ObjectUserAccount account = PlayerAccountTable.getAccountByUUID(snapshot.uuid());
        if (account == null) {
            return ObjectUserAccount.fromSnapshot(snapshot.username(), snapshot.email(), snapshot.uuid(), snapshot.discordID(), snapshot.discordAuthCode(), snapshot.administrator());
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
        final String headerToken = handler.header("X-Photon-User-Token");
        if (headerToken != null && !headerToken.isBlank()) return headerToken.trim();

        final String authorization = handler.header("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }

        return null;
    }

    private static AccountSnapshot snapshot(ObjectUserAccount account) {
        return new AccountSnapshot(
            account.getUsername(),
            account.getEmail(),
            account.getUuid(),
            account.getDiscordID(),
            account.getDiscordAuthCode(),
            account.isAdministrator()
        );
    }
}

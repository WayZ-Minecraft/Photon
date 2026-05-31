package niwer.photon.web;

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
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;

public final class UserSessionManager {

    private static final File SESSION_FILE = new File(Directories.BASE_DIR, "user_sessions.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SESSION_MAP_TYPE = new TypeToken<Map<String, Session>>() {}.getType();
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private UserSessionManager() {}

    public record AuthSession(String token, ObjectPlayerAccount account) {}

    private record AccountSnapshot(String username, String email, String uuid, String discordID, String discordAuthCode) {}

    private record Session(AccountSnapshot account, long createdAt) {}

    public static void load() {
        SESSIONS.clear();
        if (!SESSION_FILE.exists()) return;

        try (FileReader reader = new FileReader(SESSION_FILE)) {
            final Map<String, Session> loadedSessions = GSON.fromJson(reader, SESSION_MAP_TYPE);
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
        final ObjectPlayerAccount account = PlayerAccountTable.getAccountByEmail(email);
                if (account == null || password == null || !PlayerAccountTable.passwordMatches(account.password(), password)) return null;

		if (!PlayerAccountTable.isArgon2Password(account.password())) {
			PlayerAccountTable.setPassword(account.getUuid(), password);
		}

        final String token = UUID.randomUUID().toString().replace("-", "");
        SESSIONS.put(token, new Session(snapshot(account), System.currentTimeMillis()));
        save();
        return new AuthSession(token, account);
    }

    public static AuthSession createSessionForAccount(ObjectPlayerAccount account) {
        if (account == null) return null;
        final String token = UUID.randomUUID().toString().replace("-", "");
        SESSIONS.put(token, new Session(snapshot(account), System.currentTimeMillis()));
        save();
        return new AuthSession(token, account);
    }

    public static ObjectPlayerAccount requireAccount(Context handler) {
        final ObjectPlayerAccount account = accountFromRequest(handler);
        if (account == null) {
            handler.status(401).result("Unauthorized");
        }
        return account;
    }

    public static ObjectPlayerAccount accountFromRequest(Context handler) {
        final String token = extractToken(handler);
        if (token == null || token.isBlank()) return null;

        final Session session = SESSIONS.get(token);
        if (session == null) return null;

        final AccountSnapshot snapshot = session.account();
        return ObjectPlayerAccount.fromSnapshot(snapshot.username(), snapshot.email(), snapshot.uuid(), snapshot.discordID(), snapshot.discordAuthCode(), false, false);
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

        final String queryToken = handler.queryParam("token");
        if (queryToken != null && !queryToken.isBlank()) return queryToken.trim();

        final String formToken = handler.formParam("token");
        if (formToken != null && !formToken.isBlank()) return formToken.trim();

        return null;
    }

    private static AccountSnapshot snapshot(ObjectPlayerAccount account) {
        return new AccountSnapshot(
            account.getUsername(),
            account.getEmail(),
            account.getUuid(),
            account.getDiscordID(),
            account.getDiscordAuthCode()
        );
    }
}
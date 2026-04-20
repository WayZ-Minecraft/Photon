package niwer.photon.web;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;

public final class AdminSessionManager {

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private AdminSessionManager() {}

    public record AuthSession(String token, ObjectPlayerAccount account) {}

    private record Session(String uuid, String email, long createdAt) {}

    public static AuthSession login(String email, String password) {
        if (email == null || password == null) return null;

        final ObjectPlayerAccount account = PlayerAccountTable.getAccountByEmail(email);
        if (account == null || account.password() == null || !account.password().equals(password) || !account.projectAuthor()) {
            return null;
        }

        final String token = UUID.randomUUID().toString().replace("-", "");
        SESSIONS.put(token, new Session(account.uuid(), account.email(), System.currentTimeMillis()));
        return new AuthSession(token, account);
    }

    public static ObjectPlayerAccount requireProjectAuthor(Context handler) {
        final ObjectPlayerAccount account = accountFromRequest(handler);
        if (account == null) {
            handler.status(401).result("Unauthorized");
            return null;
        }

        if (!account.projectAuthor()) {
            handler.status(403).result("Project author access required");
            return null;
        }

        return account;
    }

    public static ObjectPlayerAccount accountFromRequest(Context handler) {
        final String token = extractToken(handler);
        if (token == null || token.isBlank()) return null;

        final Session session = SESSIONS.get(token);
        if (session == null) return null;

        final ObjectPlayerAccount account = PlayerAccountTable.getAccountByUUID(session.uuid());
        if (account == null) {
            SESSIONS.remove(token);
            return null;
        }

        return account;
    }

    public static void logout(String token) {
        if (token != null && !token.isBlank()) {
            SESSIONS.remove(token);
        }
    }

    private static String extractToken(Context handler) {
        final String authorization = handler.header("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }

        final String headerToken = handler.header("X-Photon-Token");
        if (headerToken != null && !headerToken.isBlank()) return headerToken.trim();

        final String queryToken = handler.queryParam("token");
        if (queryToken != null && !queryToken.isBlank()) return queryToken.trim();

        final String formToken = handler.formParam("token");
        if (formToken != null && !formToken.isBlank()) return formToken.trim();

        return null;
    }
}
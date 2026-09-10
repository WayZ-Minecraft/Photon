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
import niwer.photon.util.HashUtils;

public final class SessionManager {

    private static final Type SESSION_MAP_TYPE = new TypeToken<Map<String, SessionSnapshot>>() {}.getType();

    public enum Scope {
        ADMIN("admin_sessions.json", "photon_admin", true),
        USER("user_sessions.json", "X-Photon-User-Token", false);

        private final File file;
        private final String headerOrCookie;
        private final boolean isCookie;
        private final Map<String, SessionSnapshot> sessions = new ConcurrentHashMap<>();

        Scope(String filename, String headerOrCookie, boolean isCookie) {
            this.file = new File(Directories.BASE_DIR, filename);
            this.headerOrCookie = headerOrCookie;
            this.isCookie = isCookie;
        }

        private synchronized void load() {
            sessions.clear();
            if (!file.exists()) return;
            try (FileReader reader = new FileReader(file)) {
                Map<String, SessionSnapshot> loaded = GsonUtils.GSON.fromJson(reader, SESSION_MAP_TYPE);
                if (loaded != null) sessions.putAll(loaded);
            } catch (Exception ignored) {}
        }

        private synchronized void save() {
            if (!Directories.BASE_DIR.exists()) Directories.BASE_DIR.mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                GsonUtils.GSON.toJson(sessions, SESSION_MAP_TYPE, writer);
            } catch (Exception ignored) {}
        }

        /**
         * Extracts the session token from the request context, either from a cookie or a header, depending on the scope's configuration.
         * If the token is not found in the expected location, it will also check for a Bearer token in the Authorization header.
         * 
         * @param handler The Javalin context containing the request and response information
         * @return The extracted session token as a String, or null if no token is found
         */
        public String extractToken(Context handler) {
            String token = isCookie ? handler.cookie(headerOrCookie) : handler.header(headerOrCookie);
            if (token != null && !token.isBlank()) return token.trim();

            String auth = handler.header("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                return auth.substring(7).trim();
            }
            return null;
        }
    }

    static {
        for (Scope scope : Scope.values()) scope.load();
    }

    private SessionManager() {}

    /**
     * Attempts to log in a user with the provided email and password, creating a new session if successful. Returns an AuthSession object containing the session token and account information, or null if login fails.
     * 
     * @param email The email address of the user attempting to log in
     * @param password The password of the user attempting to log in
     * @param scope The scope of the session (ADMIN or USER) to determine which session map to use
     * @return An AuthSession object containing the session token and account information if login is successful; null otherwise
     */
    public static Session login(String email, String password, Scope scope) {
        if (email == null || password == null) return null;

        ObjectUserAccount account = PlayerAccountTable.getAccountByEmail(email);
        if (account == null || account.password() == null) return null;
        if (scope == Scope.ADMIN && !account.isAdministrator()) return null;
        if (!HashUtils.passwordMatches(account.password(), password)) return null;

        return createSession(account, scope);
    }

    private static Session createSession(ObjectUserAccount account, Scope scope) {
        if (account == null) return null;

        String token = UUID.randomUUID().toString();
        String csrf = (scope == Scope.ADMIN) ? UUID.randomUUID().toString() : null;

        scope.sessions.put(token, new SessionSnapshot(account, System.currentTimeMillis(), csrf));
        scope.save();
        return new Session(token, account);
    }

    private static ObjectUserAccount accountFromRequest(Context handler, Scope scope) {
        String token = scope.extractToken(handler);
        SessionSnapshot session = (token != null) ? scope.sessions.get(token) : null;

        if (session == null) {
            // Admins can fall back to regular user session if the account has admin privileges
            if (scope == Scope.ADMIN) {
                ObjectUserAccount userAccount = accountFromRequest(handler, Scope.USER);
                return (userAccount != null && userAccount.isAdministrator()) ? userAccount : null;
            }
            return null;
        }

        ObjectUserAccount snapshot = session.account();
        if (snapshot == null || snapshot.getUuid() == null) return null;

        ObjectUserAccount freshAccount = PlayerAccountTable.getAccountByUUID(snapshot.getUuid());
        return (freshAccount != null) ? freshAccount : snapshot;
    }

    /**
     * Requires that the request is made by a logged-in user. If the request is not made by a logged-in user, it will respond with a 401 Unauthorized status code and return null.
     * 
     * @param handler The Javalin context containing the request and response information
     * @return The ObjectUserAccount of the user making the request, or null if the request is not made by a logged-in user
     */
    public static ObjectUserAccount requireAccount(Context handler) {
        ObjectUserAccount account = accountFromRequest(handler, Scope.USER);
        if (account == null) handler.status(401).result("Unauthorized");
        return account;
    }

    /**
     * Requires that the request is made by an administrator. If the request is not made by an administrator, it will respond with a 401 Unauthorized or 403 Forbidden status code and return null.
     * 
     * @param handler The Javalin context containing the request and response information
     * @return The ObjectUserAccount of the administrator making the request, or null if the request is not made by an administrator
     */
    public static ObjectUserAccount requireAdministrator(Context handler) {
        ObjectUserAccount account = accountFromRequest(handler, Scope.ADMIN);
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

    /**
     * Logs out the session associated with the given token and scope.
     * 
     * @param token The session token to invalidate
     * @param scope The scope of the session (ADMIN or USER) to determine which session map to modify
     */
    public static void logout(String token, Scope scope) {
        if (token != null && scope.sessions.remove(token) != null) scope.save();
    }

    /**
     * Retrieves the CSRF token associated with a given session token.
     * 
     * @param token The session token for which to retrieve the CSRF token
     * @return The CSRF token associated with the session, or null if the session does not exist or the token is invalid
     */
    public static String getCsrfForToken(String token) {
        if (token == null || token.isBlank()) return null;
        SessionSnapshot session = Scope.ADMIN.sessions.get(token);
        return (session != null) ? session.csrf() : null;
    }

    /**
     * Validates the CSRF token provided in the request against the CSRF token stored in the session associated with the request's token.
     * 
     * @param handler The Javalin context containing the request and response information
     * @return True if the CSRF token is valid and matches the session's CSRF token; false otherwise
     */
    public static boolean validateCsrf(Context handler) {
        String token = Scope.ADMIN.extractToken(handler);
        if (token == null) return false;

        SessionSnapshot session = Scope.ADMIN.sessions.get(token);
        if (session == null || session.csrf() == null) return false;

        String header = handler.header("X-CSRF-Token");
        return session.csrf().equals(header);
    }
}
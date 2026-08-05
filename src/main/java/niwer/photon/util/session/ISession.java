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

public abstract class ISession {

    private final Type SESSION_MAP_TYPE = new TypeToken<Map<String, SessionSnapshot>>() {}.getType();
    protected final Map<String, SessionSnapshot> SESSIONS = new ConcurrentHashMap<>();

    private final File sessionFile;
    private final boolean isAdmin;

    public ISession(String fileName, boolean isAdmin) {
        this.sessionFile = new File(Directories.SESSIONS_DIR, fileName + ".json");
        this.isAdmin = isAdmin;

        this.load(); // Load sessions from the file when the session manager is initialized
    }

    /**
     * Validates the CSRF token for the current session based on the request context.
     * 
     * @param handler The Javalin context containing the request information
     * @return true if the CSRF token is valid, false otherwise
     */
    public final boolean validateCsrf(Context handler) {
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
    
    /**
     * Retrieves the CSRF token associated with the given session token.
     * 
     * @param token The session token for which to retrieve the CSRF token
     * @return The CSRF token associated with the session, or null if not found
     */
    public final String getCsrfForToken(String token) {
        if (token == null || token.isBlank()) return null;

        /* Retrieve the session snapshot for the given token */
        final SessionSnapshot session = SESSIONS.get(token);
        if (session == null) return null;

        return session.csrf();
    }

    protected final String extractToken(Context handler) {
        if(this.isAdmin) {
            // Prefer cookie-based sessions (HttpOnly) to avoid exposing tokens to JS/localStorage
            try {
                final String cookieToken = handler.cookie("photon_admin");
                if (cookieToken != null && !cookieToken.isBlank()) return cookieToken.trim();
            } catch (Exception ignored) {}
    
            // Fallback to bearer Authorization header only. Do NOT accept tokens from query/form parameters.
            final String authorization = handler.header("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) return authorization.substring("Bearer ".length()).trim();

            return null;
        }

        final String headerToken = handler.header("X-Photon-User-Token");
        if (headerToken != null && !headerToken.isBlank()) return headerToken.trim();

        final String authorization = handler.header("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) return authorization.substring("Bearer ".length()).trim();

        return null;
    }

    private synchronized final void load() {
        SESSIONS.clear(); // Clear existing sessions before loading new ones
        if (!sessionFile.exists()) return;

        try (var reader = new FileReader(sessionFile)) {
            final Map<String, SessionSnapshot> loadedSessions = GsonUtils.GSON.fromJson(reader, SESSION_MAP_TYPE);
            if (loadedSessions != null) SESSIONS.putAll(loadedSessions);
        } catch (Exception ignored) {}
    }

    private synchronized final void save() {
        if (!Directories.SESSIONS_DIR.exists()) Directories.SESSIONS_DIR.mkdirs();

        try (FileWriter writer = new FileWriter(sessionFile)) {
            GsonUtils.GSON.toJson(SESSIONS, SESSION_MAP_TYPE, writer);
        } catch (Exception ignored) {}
    }

    /**
     * Logs out a session by removing it from the session map and saving the updated sessions to the file.
     * 
     * @param token The session token to be logged out
     */
    public final void logout(String token) {
        if (token != null && !token.isBlank()) {
            SESSIONS.remove(token);
            save();
        }
    }

    /**
     * Logs in a user by validating their email and password, creating a new session token, and saving the session to the file.
     * 
     * @param email The user's email address
     * @param password The user's password
     * @return An AuthSession object containing the session token and user account information, or null if login fails
     */
    public final AuthSession login(String email, String password) {
        if (email == null || password == null) return null;

        /* Try to get the account */
        final ObjectUserAccount ACCOUNT = PlayerAccountTable.getAccountByEmail(email);
        if (ACCOUNT == null || ACCOUNT.password() == null || !PlayerAccountTable.passwordMatches(ACCOUNT.password(), password)) return null;
    
        /* Update the password if it's not using Argon2 */
        if(!PlayerAccountTable.isArgon2Password(ACCOUNT.password())) PlayerAccountTable.setPassword(ACCOUNT.getUuid(), password);
    
        /* Generate a new session token */
        final String TOKEN = UUID.randomUUID().toString().replace("-", "");
        
        /* Generate a new CSRF token if the user is an administrator */
        final String CSRF = isAdmin && ACCOUNT.isAdministrator() ? UUID.randomUUID().toString().replace("-", "") : null;

        /* Create the session snapshot */
        SESSIONS.put(TOKEN, new SessionSnapshot(ACCOUNT, System.currentTimeMillis(), CSRF));
        save();
        return new AuthSession(password, ACCOUNT);
    }

    /**
     * Retrieves the user account associated with the current request context, ensuring that the user is authenticated.
     * 
     * @param handler The Javalin context containing the request information
     * @return The ObjectUserAccount of the authenticated user, or null if the user is not authenticated
     */
    public final ObjectUserAccount requireAccount(Context handler) {
        final ObjectUserAccount account = accountFromRequest(handler);
        if (account == null) handler.status(401).result("Unauthorized");
        return account;
    }

    /**
     * Retrieves the user account associated with the current request context, ensuring that the user is an administrator.
     * 
     * @param handler The Javalin context containing the request information
     * @return The ObjectUserAccount of the administrator, or null if the user is not an administrator or not authenticated
     */
    public final ObjectUserAccount requireAdministrator(Context handler) {
        final ObjectUserAccount account = requireAccount(handler); // Also checks for null and sets 401 if not authenticated
        if (!account.isAdministrator()) {
            handler.status(403).result("Administrator access required");
            return null;
        }

        return account;
    }

    public abstract ObjectUserAccount accountFromRequest(Context handler);
}

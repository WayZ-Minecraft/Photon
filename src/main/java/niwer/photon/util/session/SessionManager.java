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

public final class SessionManager {

    public enum Scope {
        ADMIN("admin_sessions.json"),
        USER("user_sessions.json");

        private final File file;
        private final Map<String, SessionSnapshot> sessions = new ConcurrentHashMap<>();

        Scope(String filename) {
            this.file = new File(Directories.BASE_DIR, filename);
        }
    }

    private static final Type SESSION_MAP_TYPE = new TypeToken<Map<String, SessionSnapshot>>() {}.getType();

    static {
        for (final Scope SCOPE : Scope.values()) load(SCOPE);
    }

    private SessionManager() {}

    public static synchronized void load(Scope scope) {
        scope.sessions.clear();
        if (!scope.file.exists()) return;

        try (FileReader reader = new FileReader(scope.file)) {
            final Map<String, SessionSnapshot> loaded = GsonUtils.GSON.fromJson(reader, SESSION_MAP_TYPE);
            if (loaded != null) scope.sessions.putAll(loaded);
        } catch (Exception ignored) {}
    }

    private static synchronized void save(Scope scope) {
        if (!Directories.BASE_DIR.exists()) Directories.BASE_DIR.mkdirs();

        try (FileWriter writer = new FileWriter(scope.file)) {
            GsonUtils.GSON.toJson(scope.sessions, SESSION_MAP_TYPE, writer);
        } catch (Exception ignored) {}
    }

    public static AuthSession login(String email, String password, Scope scope) {
        if (email == null || password == null) return null;

        final ObjectUserAccount account = PlayerAccountTable.getAccountByEmail(email);
        if (account == null || account.password() == null) return null;
        if (scope == Scope.ADMIN && !account.isAdministrator()) return null;
        if (!PlayerAccountTable.passwordMatches(account.password(), password)) return null;

        if (!PlayerAccountTable.isArgon2Password(account.password())) {
            PlayerAccountTable.setPassword(account.getUuid(), password);
        }

        return createSession(account, scope);
    }

    public static AuthSession createSession(ObjectUserAccount account, Scope scope) {
        if (account == null) return null;

        final String token = UUID.randomUUID().toString().replace("-", "");
        final String csrf = (scope == Scope.ADMIN) ? UUID.randomUUID().toString().replace("-", "") : null;

        scope.sessions.put(token, new SessionSnapshot(account, System.currentTimeMillis(), csrf));
        save(scope);
        return new AuthSession(token, account);
    }

    private static ObjectUserAccount accountFromRequest(Context handler, Scope scope) {
        final String token = extractToken(handler, scope);
        if (token == null || token.isBlank()) {
            // Admin can fall back to checking user credentials if admin-capable
            if (scope == Scope.ADMIN) {
                final ObjectUserAccount userAccount = accountFromRequest(handler, Scope.USER);
                return (userAccount != null && userAccount.isAdministrator()) ? userAccount : null;
            }
            return null;
        }

        final SessionSnapshot session = scope.sessions.get(token);
        if (session == null) {
            if (scope == Scope.ADMIN) {
                final ObjectUserAccount userAccount = accountFromRequest(handler, Scope.USER);
                return (userAccount != null && userAccount.isAdministrator()) ? userAccount : null;
            }
            return null;
        }

        final ObjectUserAccount snapshot = session.account();
        if (snapshot == null || snapshot.getUuid() == null || snapshot.getUuid().isBlank()) return snapshot;

        final ObjectUserAccount account = PlayerAccountTable.getAccountByUUID(snapshot.getUuid());
        return account;
    }

    public static ObjectUserAccount requireAccount(Context handler) {
        final ObjectUserAccount account = accountFromRequest(handler, Scope.USER);
        if (account == null) handler.status(401).result("Unauthorized");
        return account;
    }

    public static ObjectUserAccount requireAdministrator(Context handler) {
        final ObjectUserAccount account = accountFromRequest(handler, Scope.ADMIN);
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

    public static void logout(String token, Scope scope) {
        if (token != null && !token.isBlank()) {
            scope.sessions.remove(token);
            save(scope);
        }
    }

    public static String getCsrfForToken(String token) {
        if (token == null || token.isBlank()) return null;
        final SessionSnapshot session = Scope.ADMIN.sessions.get(token);
        return (session != null) ? session.csrf() : null;
    }

    public static boolean validateCsrf(Context handler) {
        try {
            final String token = extractToken(handler, Scope.ADMIN);
            if (token == null || token.isBlank()) return false;

            final SessionSnapshot session = Scope.ADMIN.sessions.get(token);
            if (session == null || session.csrf() == null || session.csrf().isBlank()) return false;

            final String header = handler.header("X-CSRF-Token");
            return header != null && header.equals(session.csrf());
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String extractToken(Context handler, Scope scope) {
        if (scope == Scope.ADMIN) {
            try {
                final String cookieToken = handler.cookie("photon_admin");
                if (cookieToken != null && !cookieToken.isBlank()) return cookieToken.trim();
            } catch (Exception ignored) {}
        } else {
            final String headerToken = handler.header("X-Photon-User-Token");
            if (headerToken != null && !headerToken.isBlank()) return headerToken.trim();
        }

        final String auth = handler.header("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring("Bearer ".length()).trim();
        }
        return null;
    }
}
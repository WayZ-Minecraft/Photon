package niwer.photon.util.session;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.web.WebServerEngine;

public final class AdminSessionManager extends ISession {

    public AdminSessionManager() { super("admin_sessions", true); }

    public ObjectUserAccount accountFromRequest(Context handler) {
        final String TOKEN = extractToken(handler);
        if (TOKEN == null || TOKEN.isBlank()) {
            final ObjectUserAccount userAccount = WebServerEngine.USER_SESSION_MANAGER.accountFromRequest(handler);
            return userAccount != null && userAccount.isAdministrator() ? userAccount : null;
        }

        final SessionSnapshot SESSION = SESSIONS.get(TOKEN);
        if (SESSION == null) {
            final ObjectUserAccount userAccount = WebServerEngine.USER_SESSION_MANAGER.accountFromRequest(handler);
            return userAccount != null && userAccount.isAdministrator() ? userAccount : null;
        }

        final ObjectUserAccount snapshot = SESSION.account();
        if (snapshot == null || snapshot.getUuid() == null || snapshot.getUuid().isBlank()) return null;

        final ObjectUserAccount account = PlayerAccountTable.getAccountByUUID(snapshot.getUuid());
        if (account == null) return snapshot;

        return account;
    }
}
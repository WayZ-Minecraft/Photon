package niwer.photon.util.session;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;

public final class UserSessionManager extends ISession {

    public UserSessionManager() { super("user_sessions", false); }

    public ObjectUserAccount accountFromRequest(Context handler) {
        final String TOKEN = extractToken(handler);
        if (TOKEN == null || TOKEN.isBlank()) return null;

        final SessionSnapshot SESSION = SESSIONS.get(TOKEN);
        if (SESSION == null) return null;

        final ObjectUserAccount snapshot = SESSION.account();
        if (snapshot == null || snapshot.getUuid() == null || snapshot.getUuid().isBlank()) return null;

        final ObjectUserAccount account = PlayerAccountTable.getAccountByUUID(snapshot.getUuid());
        if (account == null) return snapshot;

        return account;
    }
}

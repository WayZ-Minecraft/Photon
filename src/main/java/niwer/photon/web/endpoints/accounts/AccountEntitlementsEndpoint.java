package niwer.photon.web.endpoints.accounts;

import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.session.SessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountEntitlementsEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/entitlements"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 10, TimeUnit.MINUTES);

        final var account = SessionManager.requireAccount(handler);
        if (account == null) return;

        handler.json(SubscriptionTable.entitlements(account.getUuid()));
    }
}
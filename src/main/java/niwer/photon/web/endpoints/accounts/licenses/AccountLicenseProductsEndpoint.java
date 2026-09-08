package niwer.photon.web.endpoints.accounts.licenses;

import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.session.UserSessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseProductsEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/license-products"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 10, TimeUnit.SECONDS);

        final var account = UserSessionManager.requireAccount(handler);
        if (account == null) return;
        if (!SubscriptionTable.hasAnyAccess(account.getUuid())) {
            handler.status(403).result("Purchase or active subscription required");
            return;
        }

        handler.json(Directories.getConfig().getLicenseProducts());
    }
}
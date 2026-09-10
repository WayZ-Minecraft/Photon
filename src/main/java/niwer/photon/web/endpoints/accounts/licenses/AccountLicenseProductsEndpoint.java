package niwer.photon.web.endpoints.accounts.licenses;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectProduct;
import niwer.photon.util.session.SessionManager;
import niwer.photon.util.stripe.EntitlementManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseProductsEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/license-products"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 10, TimeUnit.SECONDS);

        final var ACCOUNT = SessionManager.requireAccount(handler);
        if (ACCOUNT == null) return;
        
        /* Check if the user has any access to the license system */
        if (!EntitlementManager.hasAnyAccess(ACCOUNT.getUuid())) {
            handler.status(403).result("Purchase or active subscription required");
            return;
        }
        
        /* Get the products to which the user has access */
        final List<ObjectProduct> PRODUCTS = Directories.getConfig().getProducts().stream().filter(p -> EntitlementManager.hasAccess(ACCOUNT.getUuid(), p.id())).toList();
        handler.json(PRODUCTS);
    }
}
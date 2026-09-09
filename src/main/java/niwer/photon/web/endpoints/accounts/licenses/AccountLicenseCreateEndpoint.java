package niwer.photon.web.endpoints.accounts.licenses;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.license.LicenseManager;
import niwer.photon.util.session.SessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.EndpointUtils;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseCreateEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 10, TimeUnit.SECONDS);

        final var ACCOUNT = SessionManager.requireAccount(handler);
        if (ACCOUNT == null) return;

        final JsonObject body = EndpointUtils.readBody(handler);
        final String requestedProductId = GsonUtils.getString(body, "product_id", "productId", null);
        final var products = Directories.getConfig().getProducts();
        final var product = products.stream()
            .filter(candidate -> Objects.equals(candidate.id(), requestedProductId) || (requestedProductId == null && candidate == products.get(0)))
            .findFirst()
            .orElse(null);

        if (product == null || product.id() == null || product.id().isBlank()) {
            handler.status(400).result("Unknown license product");
            return;
        }

        final String productId = product.id();
        if (!SubscriptionTable.hasAccess(ACCOUNT.getEmail(), ACCOUNT.getUuid(), productId)) {
            handler.status(403).result("Purchase or active subscription required for this product");
            return;
        }
        final String name = GsonUtils.getString(body, "name", "name", ACCOUNT.getUsername());
        final Long durationDays = GsonUtils.getLong(body, "duration_days", "durationDays", product.defaultLicenseDurationDays());
        final Long expiresAt = GsonUtils.getLong(body, "expires_at", "expiresAt", null);

        final Long computedExpiresAt = expiresAt != null ? expiresAt : (durationDays == null || durationDays <= 0L ? null : System.currentTimeMillis() + (durationDays * 86400000L));
        final ObjectLicense license = LicenseManager.issueLicense(productId, name, ACCOUNT.getEmail(), ACCOUNT.getUuid(), computedExpiresAt);
        if(license == null) {
            handler.status(500).result("Failed to create license");
            return;
        }
        
        handler.status(200).json(license.payload());
    }
}
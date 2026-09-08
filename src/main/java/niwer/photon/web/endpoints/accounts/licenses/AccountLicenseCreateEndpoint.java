package niwer.photon.web.endpoints.accounts.licenses;

import java.util.concurrent.TimeUnit;
import java.util.Objects;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.license.LicenseManager;
import niwer.photon.util.session.UserSessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.EndpointUtils;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseCreateEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 10, TimeUnit.SECONDS);

        final var account = UserSessionManager.requireAccount(handler);
        if (account == null) return;
        final JsonObject body = EndpointUtils.readBody(handler);
        final String requestedProductId = GsonUtils.getString(body, "product_id", "productId", null);
        final var products = Directories.getConfig().getLicenseProducts();
        final var product = products.stream()
            .filter(candidate -> Objects.equals(candidate.id, requestedProductId) || (requestedProductId == null && candidate == products.get(0)))
            .findFirst()
            .orElse(null);

        if (product == null || product.id == null || product.id.isBlank()) {
            handler.status(400).result("Unknown license product");
            return;
        }

        final String productId = product.id;
        if (!SubscriptionTable.hasAccess(account.getEmail(), account.getUuid(), productId)) {
            handler.status(403).result("Purchase or active subscription required for this product");
            return;
        }
        final String name = GsonUtils.getString(body, "name", "name", account.getUsername());
        final Long durationDays = GsonUtils.getLong(body, "duration_days", "durationDays", product.default_license_duration_days);
        final Long expiresAt = GsonUtils.getLong(body, "expires_at", "expiresAt", null);

        final Long computedExpiresAt = expiresAt != null ? expiresAt : (durationDays == null || durationDays <= 0L ? null : System.currentTimeMillis() + (durationDays * 86400000L));
        final ObjectLicense license = LicenseManager.issueLicense(productId, name, account.getEmail(), account.getUuid(), computedExpiresAt);
        if(license == null) {
            handler.status(500).result("Failed to create license");
            return;
        }
        
        handler.status(200).json(license.payload());
    }
}
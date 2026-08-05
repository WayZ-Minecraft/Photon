package niwer.photon.web.endpoints.accounts.licenses;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.license.LicenseManager;
import niwer.photon.web.WebServerEngine;
import niwer.photon.web.endpoints.EndpointUtils;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseCreateEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final var account = WebServerEngine.USER_SESSION_MANAGER.requireAccount(handler);
        if (account == null) return;
        if (!SubscriptionTable.isActive(account.getEmail(), account.getUuid())) {
            handler.status(403).result("Active subscription required");
            return;
        }

        final JsonObject body = EndpointUtils.readBody(handler);
        final String productId = GsonUtils.getString(body, "product_id", "productId", Directories.getConfig().license_product_id);
        final String name = GsonUtils.getString(body, "name", "name", account.getUsername());
        final Long durationDays = GsonUtils.getLong(body, "duration_days", "durationDays", Directories.getConfig().license_default_duration_days);
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
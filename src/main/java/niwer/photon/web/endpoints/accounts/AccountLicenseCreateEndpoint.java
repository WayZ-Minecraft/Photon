package niwer.photon.web.endpoints.accounts;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.license.LicenseManager;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseCreateEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final var account = UserSessionManager.requireAccount(handler);
        if (account == null) return;
        if (!SubscriptionTable.isActive(account.getEmail(), account.getUuid())) {
            handler.status(403).result("Active subscription required");
            return;
        }

        final JsonObject body = readBody(handler);
        final String productId = getString(body, "product_id", "productId", Directories.getConfig().license_product_id);
        final String customerName = getString(body, "customer_name", "customerName", account.getUsername());
        final Long durationDays = getLong(body, "duration_days", "durationDays", Directories.getConfig().license_default_duration_days);
        final Long expiresAt = getLong(body, "expires_at", "expiresAt", null);
        final String orderId = getString(body, "order_id", "orderId", "");

        final Long computedExpiresAt = expiresAt != null ? expiresAt : (durationDays == null || durationDays <= 0L ? null : System.currentTimeMillis() + (durationDays * 86400000L));
        final ObjectLicense license = LicenseManager.issueLicense(productId, customerName, account.getEmail(), orderId, computedExpiresAt);
        handler.status(200).json(license);
    }

    private static JsonObject readBody(Context handler) {
        try {
            return Directories.GSON.fromJson(handler.body(), JsonObject.class);
        } catch (Exception ignored) {
            return new JsonObject();
        }
    }

    private static String getString(JsonObject body, String primaryKey, String secondaryKey, String defaultValue) {
        if (body != null && body.has(primaryKey) && !body.get(primaryKey).isJsonNull()) return body.get(primaryKey).getAsString();
        if (body != null && body.has(secondaryKey) && !body.get(secondaryKey).isJsonNull()) return body.get(secondaryKey).getAsString();
        return defaultValue;
    }

    private static Long getLong(JsonObject body, String primaryKey, String secondaryKey, Long defaultValue) {
        try {
            if (body != null && body.has(primaryKey) && !body.get(primaryKey).isJsonNull()) return body.get(primaryKey).getAsLong();
            if (body != null && body.has(secondaryKey) && !body.get(secondaryKey).isJsonNull()) return body.get(secondaryKey).getAsLong();
        } catch (Exception ignored) {
            return defaultValue;
        }
        return defaultValue;
    }
}
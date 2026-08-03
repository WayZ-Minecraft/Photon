package niwer.photon.web.endpoints.accounts;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.license.LicenseManager;
import niwer.photon.util.session.UserSessionManager;
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
        final String name = getString(body, "name", "name", account.getUsername());
        final Long durationDays = getLong(body, "duration_days", "durationDays", Directories.getConfig().license_default_duration_days);
        final Long expiresAt = getLong(body, "expires_at", "expiresAt", null);

        final Long computedExpiresAt = expiresAt != null ? expiresAt : (durationDays == null || durationDays <= 0L ? null : System.currentTimeMillis() + (durationDays * 86400000L));
        Console.log("hey " + computedExpiresAt).send();
        final ObjectLicense license = LicenseManager.issueLicense(productId, name, account.getEmail(), account.getUuid(), computedExpiresAt);
        handler.status(200).json(toPayload(license));
    }

    private static Map<String, Object> toPayload(ObjectLicense license) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        final Long createdAt = license.createdAt() == null ? null : license.createdAt().getTime();
        final Long activatedAt = license.activatedAt() == null ? null : license.activatedAt().getTime();
        final Long expiresAt = license.expiresAt() == null ? null : license.expiresAt().getTime();

        payload.put("licenseKey", license.licenseKey());
        payload.put("license_key", license.licenseKey());
        payload.put("productId", license.productId());
        payload.put("product_id", license.productId());
        payload.put("name", license.name());
        payload.put("customerEmail", license.customerEmail());
        payload.put("customer_email", license.customerEmail());
        payload.put("creatorUuid", license.creatorUuid());
        payload.put("creator_uuid", license.creatorUuid());
        payload.put("hwid", license.hwid());
        payload.put("status", license.status());
        payload.put("createdAt", createdAt);
        payload.put("created_at", createdAt);
        payload.put("activatedAt", activatedAt);
        payload.put("activated_at", activatedAt);
        payload.put("expiresAt", expiresAt);
        payload.put("expires_at", expiresAt);
        return payload;
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
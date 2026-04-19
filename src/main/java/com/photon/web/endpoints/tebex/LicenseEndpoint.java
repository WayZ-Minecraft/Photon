package com.photon.web.endpoints.tebex;

import com.google.gson.JsonObject;
import com.photon.Directories;
import com.photon.objects.ObjectLicense;
import com.photon.util.license.LicenseManager;
import com.photon.web.endpoints.IEndpoint;

import io.javalin.http.Context;

/**
 * Endpoint to handle Tebex webhook for license issuance. This endpoint will be called by Tebex when a new purchase is made, and it will create a new license in the database based on the provided information.
 * 
 * @author Niwer
 */
public class LicenseEndpoint implements IEndpoint {

    @Override
    public String path() {
        return "/tebex/license";
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public void handle(Context handler) {
        if (!isValidTebexSecret(handler.header("X-Photon-Secret"), handler.queryParam("secret"))) {
            handler.status(401).result("Unauthorized");
            return;
        }

        final JsonObject body = Directories.GSON.fromJson(handler.body(), JsonObject.class);
        if (body == null) {
            handler.status(400).result("Invalid JSON body");
            return;
        }

        final String productId = getString(body, "product_id", "productId", Directories.getConfig().license_product_id);
        final String customerName = getString(body, "customer_name", "customerName", "");
        final String customerEmail = getString(body, "customer_email", "customerEmail", "");
        final String tebexOrderId = getString(body, "tebex_order_id", "order_id", "");
        final Long expiresAt = getLong(body, "expires_at", "expiresAt", null);
        final Long durationDays = getLong(body, "duration_days", "durationDays", Directories.getConfig().license_default_duration_days);

        final Long computedExpiresAt = expiresAt != null ? expiresAt : (durationDays == null || durationDays <= 0L ? null : System.currentTimeMillis() + (durationDays * 86400000L));
        final ObjectLicense license = LicenseManager.issueLicense(productId, customerName, customerEmail, tebexOrderId, computedExpiresAt);
        handler.status(200).json(license);
    }

    private static boolean isValidTebexSecret(String headerSecret, String querySecret) {
        final String expected = Directories.getConfig().tebex_webhook_secret;
        if (expected == null || expected.isBlank()) return true;
        return expected.equals(headerSecret) || expected.equals(querySecret);
    }

    private static String getString(JsonObject body, String primaryKey, String secondaryKey, String defaultValue) {
        if (body.has(primaryKey) && !body.get(primaryKey).isJsonNull()) return body.get(primaryKey).getAsString();
        if (body.has(secondaryKey) && !body.get(secondaryKey).isJsonNull()) return body.get(secondaryKey).getAsString();
        return defaultValue;
    }

    private static Long getLong(JsonObject body, String primaryKey, String secondaryKey, Long defaultValue) {
        try {
            if (body.has(primaryKey) && !body.get(primaryKey).isJsonNull()) return body.get(primaryKey).getAsLong();
            if (body.has(secondaryKey) && !body.get(secondaryKey).isJsonNull()) return body.get(secondaryKey).getAsLong();
        } catch (Exception e) {
            return defaultValue;
        }
        return defaultValue;
    }
}

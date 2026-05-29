package niwer.photon.web.endpoints.tebex;

import java.util.Date;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.web.endpoints.IEndpoint;

public class SubscriptionEndpoint implements IEndpoint {

    @Override public String path() { return "/tebex/subscription"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        if (!isValidTebexSecret(handler.header("X-Photon-Secret"), handler.queryParam("secret"))) {
            handler.status(401).result("Unauthorized");
            return;
        }

        final JsonObject body = readBody(handler);
        if (body == null) {
            handler.status(400).result("Invalid JSON body");
            return;
        }

        final String eventType = getString(body, "event_type", "type", getString(body, "event", "action", "")).toLowerCase();
        final String customerEmail = getString(body, "customer_email", "email", null);
        if (customerEmail == null || customerEmail.isBlank()) {
            handler.status(400).result("Missing customer email");
            return;
        }

        final String customerName = getString(body, "customer_name", "customerName", "");
        final String tebexCustomerId = getString(body, "customer_id", "customerId", "");
        final String tebexSubscriptionId = getString(body, "subscription_id", "subscriptionId", "");
        final Long expiresAt = getLong(body, "expires_at", "expiresAt", null);

        final boolean expired = eventType.contains("expired");
        final SubscriptionStatus status = expired ? SubscriptionStatus.EXPIRED : SubscriptionStatus.ACTIVE;
        final Long computedExpiresAt = expired ? expiresAt : (expiresAt != null ? expiresAt : computeDefaultExpiresAt());

        final ObjectSubscription subscription = SubscriptionTable.upsertSubscription(
            customerEmail,
            customerName,
            tebexCustomerId,
            tebexSubscriptionId,
            status,
            computedExpiresAt == null ? null : new Date(computedExpiresAt)
        );

        handler.json(subscription);
    }

    private static Long computeDefaultExpiresAt() {
        final long durationDays = Directories.getConfig().license_default_duration_days;
        if (durationDays <= 0L) return null;
        return System.currentTimeMillis() + (durationDays * 86400000L);
    }

    private static boolean isValidTebexSecret(String headerSecret, String querySecret) {
        final String expected = Directories.getConfig().tebex_webhook_secret;
        if (expected == null || expected.isBlank()) return true;
        return expected.equals(headerSecret) || expected.equals(querySecret);
    }

    private static JsonObject readBody(Context handler) {
        try {
            return Directories.GSON.fromJson(handler.body(), JsonObject.class);
        } catch (Exception ignored) {
            return null;
        }
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
        } catch (Exception ignored) {
            return defaultValue;
        }
        return defaultValue;
    }
}
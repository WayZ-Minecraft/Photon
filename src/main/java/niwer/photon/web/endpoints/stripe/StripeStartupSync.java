package niwer.photon.web.endpoints.stripe;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.PhotonLogTypes;

public final class StripeStartupSync {

    private static final int PAGE_SIZE = 100;

    private StripeStartupSync() {}

    public static void run(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            Console.log("Stripe startup sync skipped: stripe_api_key is not configured").type(PhotonLogTypes.STRIPE).container(PhotonEngine.LOGGER).send();
            return;
        }

        int seenSubscriptions = 0;
        int upserted = 0;
        int skippedNoEmail = 0;
        int errors = 0;

        try {
            final Map<String, JsonObject> latestByEmail = new LinkedHashMap<>();
            String startingAfter = null;

            while (true) {
                final JsonObject page = StripeSupport.listSubscriptionsPage(apiKey, startingAfter, PAGE_SIZE);
                if (page == null) break;

                final JsonArray data = getArray(page, "data");
                if (data == null || data.isEmpty()) break;

                for (JsonElement element : data) {
                    if (element == null || !element.isJsonObject()) continue;

                    seenSubscriptions++;
                    final JsonObject subscription = element.getAsJsonObject();
                    final StripeSupport.CustomerPayload customer = StripeSupport.resolveCustomerFromSubscription(apiKey, subscription);
                    if (customer.email() == null || customer.email().isBlank()) {
                        skippedNoEmail++;
                        continue;
                    }

                    final String normalizedEmail = SubscriptionTable.normalizeEmail(customer.email());
                    if (normalizedEmail == null || normalizedEmail.isBlank()) {
                        skippedNoEmail++;
                        continue;
                    }

                    latestByEmail.putIfAbsent(normalizedEmail, subscription);
                }

                if (!StripeSupport.getBoolean(page, "has_more")) break;
                startingAfter = GsonUtils.getString(data.get(data.size() - 1).getAsJsonObject(), "id");
                if (startingAfter == null || startingAfter.isBlank()) break;
            }

            for (JsonObject subscription : latestByEmail.values()) {
                try {
                    final StripeSupport.CustomerPayload customer = StripeSupport.resolveCustomerFromSubscription(apiKey, subscription);
                    final String email = SubscriptionTable.normalizeEmail(customer.email());
                    final String subscriptionId = GsonUtils.getString(subscription, "id");

                    if (email == null || email.isBlank() || subscriptionId == null || subscriptionId.isBlank()) {
                        skippedNoEmail++;
                        continue;
                    }

                    final long periodEnd = GsonUtils.getLong(subscription, "current_period_end") * 1000L;
                    final SubscriptionStatus status = StripeSupport.stripeStatusToLocal(GsonUtils.getString(subscription, "status"));

                    SubscriptionTable.upsertSubscription(
                        email,
                        customer.name(),
                        customer.customerId(),
                        subscriptionId,
                        status,
                        periodEnd == 0L ? null : new Date(periodEnd)
                    );
                    upserted++;
                } catch (Exception e) {
                    errors++;
                }
            }

            Console.log("Stripe startup sync finished: subscriptions=" + seenSubscriptions + ", upserted=" + upserted + ", skippedNoEmail=" + skippedNoEmail + ", errors=" + errors).type(PhotonLogTypes.STRIPE).container(PhotonEngine.LOGGER).send();
        } catch (Exception e) {
            Console.log("Stripe startup sync failed: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
        }
    }

    private static JsonArray getArray(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return null;
        try {
            return object.getAsJsonArray(key);
        } catch (Exception ignored) {
            return null;
        }
    }
}

package niwer.photon.web.endpoints.stripe;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;

final class StripeSupport {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private StripeSupport() {}

    protected static JsonObject resolveSubscription(String apiKey, String payload) {
        final String subscriptionId = extractDataObjectId(payload);
        return subscriptionId == null || subscriptionId.isBlank() ? null : resolveSubscriptionById(apiKey, subscriptionId);
    }

    protected static JsonObject resolveInvoice(String apiKey, String payload) {
        final String invoiceId = extractDataObjectId(payload);
        return invoiceId == null || invoiceId.isBlank() ? null : resolveInvoiceById(apiKey, invoiceId);
    }

    protected static JsonObject resolveSubscriptionById(String apiKey, String subscriptionId) {
        return subscriptionId == null || subscriptionId.isBlank() ? null : getStripeObject(apiKey, "/v1/subscriptions/" + urlEncode(subscriptionId));
    }

    protected static JsonObject resolveInvoiceById(String apiKey, String invoiceId) {
        return invoiceId == null || invoiceId.isBlank() ? null : getStripeObject(apiKey, "/v1/invoices/" + urlEncode(invoiceId));
    }

    protected static JsonObject listSubscriptionsPage(String apiKey, String startingAfter, int limit) {
        final StringBuilder path = new StringBuilder("/v1/subscriptions?status=all&limit=").append(Math.max(1, Math.min(limit, 100)));
        if (startingAfter != null && !startingAfter.isBlank()) {
            path.append("&starting_after=").append(urlEncode(startingAfter));
        }
        return getStripeObject(apiKey, path.toString());
    }

    protected static CustomerPayload resolveCustomerFromSubscription(String apiKey, JsonObject subscription) {
        final CustomerPayload customerPayload = resolveCustomer(
            apiKey,
            getString(subscription, "customer"),
            getObject(subscription, "metadata"),
            null
        );
        if (customerPayload.email() != null && !customerPayload.email().isBlank()) return customerPayload;

        final String latestInvoiceId = getString(subscription, "latest_invoice");
        if (latestInvoiceId != null && !latestInvoiceId.isBlank()) {
            final JsonObject latestInvoice = resolveInvoiceById(apiKey, latestInvoiceId);
            if (latestInvoice != null) {
                final CustomerPayload invoicePayload = resolveCustomer(
                    apiKey,
                    getString(latestInvoice, "customer"),
                    getObject(latestInvoice, "metadata"),
                    getString(latestInvoice, "customer_email")
                );
                if (invoicePayload.email() != null && !invoicePayload.email().isBlank()) return invoicePayload;
            }
        }

        return customerPayload;
    }

    protected static CustomerPayload resolveCustomer(String apiKey, String customerId, JsonObject metadata, String directEmail) {
        final String emailFromMetadata = extractEmail(metadata);
        final String email = firstNonBlank(directEmail, emailFromMetadata);
        if (customerId == null || customerId.isBlank()) return new CustomerPayload(email, null, null);

        final JsonObject customer = getStripeObject(apiKey, "/v1/customers/" + urlEncode(customerId));
        if (customer == null) return new CustomerPayload(email, null, customerId);

        return new CustomerPayload(
            firstNonBlank(email, getString(customer, "email"), extractEmail(getObject(customer, "metadata"))),
            firstNonBlank(getString(customer, "name"), ""),
            customerId
        );
    }

    protected static String extractEmail(JsonObject metadata) {
        if (metadata == null || metadata.size() == 0) return null;
        return firstNonBlank(getString(metadata, "customer_email"), getString(metadata, "email"), getString(metadata, "customerEmail"));
    }

    protected static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private static JsonObject getStripeObject(String apiKey, String path) {
        if (apiKey == null || apiKey.isBlank() || path == null || path.isBlank()) return null;

        try {
            final HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.stripe.com" + path))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

            final HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) return null;

            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (IOException | InterruptedException | IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String extractDataObjectId(String payload) {
        final JsonObject root = parseJsonObject(payload);
        final JsonObject data = getObject(root, "data");
        final JsonObject object = getObject(data, "object");
        return getString(object, "id");
    }

    private static JsonObject parseJsonObject(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return null;
        try {
            return JsonParser.parseString(rawJson).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }

    protected static JsonObject getObject(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return null;
        try {
            return object.getAsJsonObject(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    protected static String getString(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return null;
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    protected static long getLong(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return 0L;
        try {
            return object.get(key).getAsLong();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    protected static JsonObject getObject(JsonObject object, String key, boolean allowNull) {
        return getObject(object, key);
    }

    protected static SubscriptionStatus stripeStatusToLocal(String stripeStatus) {
        if (stripeStatus == null || stripeStatus.isBlank()) return SubscriptionStatus.EXPIRED;

        final String normalized = stripeStatus.toLowerCase();
        if (normalized.equals("active") || normalized.equals("trialing") || normalized.equals("past_due")) return SubscriptionStatus.ACTIVE;

        return SubscriptionStatus.EXPIRED;
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    protected static int getInt(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return 0;
        try {
            return object.get(key).getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }

    protected static boolean getBoolean(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return false;
        try {
            return object.get(key).getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }

    protected static record CustomerPayload(String email, String name, String customerId) {}
}
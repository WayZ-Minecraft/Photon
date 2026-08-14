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
import niwer.photon.util.GsonUtils;
import niwer.photon.web.endpoints.EndpointUtils;

public final class StripeSupport {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private StripeSupport() {}

    protected static JsonObject resolveSubscription(String apiKey, String payload) {
        final String SUBSCRIBTION_ID = extractDataObjectId(payload);
        return SUBSCRIBTION_ID == null || SUBSCRIBTION_ID.isBlank() ? null : resolveSubscriptionById(apiKey, SUBSCRIBTION_ID);
    }

    protected static JsonObject resolveInvoice(String apiKey, String payload) {
        final String invoiceId = extractDataObjectId(payload);
        return invoiceId == null || invoiceId.isBlank() ? null : resolveInvoiceById(apiKey, invoiceId);
    }

	protected static JsonObject resolveCheckoutSession(String apiKey, String payload) {
		final String checkoutSessionId = extractDataObjectId(payload);
		return checkoutSessionId == null || checkoutSessionId.isBlank() ? null : resolveCheckoutSessionById(apiKey, checkoutSessionId);
	}

    protected static JsonObject resolveSubscriptionById(String apiKey, String subscriptionId) {
        return subscriptionId == null || subscriptionId.isBlank() ? null : getStripeObject(apiKey, "/v1/subscriptions/" + urlEncode(subscriptionId));
    }

    protected static JsonObject resolveInvoiceById(String apiKey, String invoiceId) {
        return invoiceId == null || invoiceId.isBlank() ? null : getStripeObject(apiKey, "/v1/invoices/" + urlEncode(invoiceId));
    }

    protected static JsonObject resolveCheckoutSessionById(String apiKey, String checkoutSessionId) {
        return checkoutSessionId == null || checkoutSessionId.isBlank() ? null : getStripeObject(apiKey, "/v1/checkout/sessions/" + urlEncode(checkoutSessionId));
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
            GsonUtils.getString(subscription, "customer"),
            GsonUtils.getObject(subscription, "metadata"),
            null
        );
        if (customerPayload.email() != null && !customerPayload.email().isBlank()) return customerPayload;

        final String latestInvoiceId = GsonUtils.getString(subscription, "latest_invoice");
        if (latestInvoiceId != null && !latestInvoiceId.isBlank()) {
            final JsonObject latestInvoice = resolveInvoiceById(apiKey, latestInvoiceId);
            if (latestInvoice != null) {
                final CustomerPayload invoicePayload = resolveCustomer(
                    apiKey,
                    GsonUtils.getString(latestInvoice, "customer"),
                    GsonUtils.getObject(latestInvoice, "metadata"),
                    GsonUtils.getString(latestInvoice, "customer_email")
                );
                if (invoicePayload.email() != null && !invoicePayload.email().isBlank()) return invoicePayload;
            }
        }

        return customerPayload;
    }

    protected static CustomerPayload resolveCustomer(String apiKey, String customerId, JsonObject metadata, String directEmail) {
        final String emailFromMetadata = extractEmail(metadata);
        final String email = EndpointUtils.firstNonBlank(directEmail, emailFromMetadata);
        if (customerId == null || customerId.isBlank()) return new CustomerPayload(email, null, null);

        final JsonObject customer = getStripeObject(apiKey, "/v1/customers/" + urlEncode(customerId));
        if (customer == null) return new CustomerPayload(email, null, customerId);

        return new CustomerPayload(
            EndpointUtils.firstNonBlank(email, GsonUtils.getString(customer, "email"), extractEmail(GsonUtils.getObject(customer, "metadata"))),
            EndpointUtils.firstNonBlank(GsonUtils.getString(customer, "name"), ""),
            customerId
        );
    }

    protected static String extractEmail(JsonObject metadata) {
        if (metadata == null || metadata.size() == 0) return null;
        return EndpointUtils.firstNonBlank(GsonUtils.getString(metadata, "customer_email"), GsonUtils.getString(metadata, "email"), GsonUtils.getString(metadata, "customerEmail"));
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
        final JsonObject root = GsonUtils.parseJsonObject(payload);
        final JsonObject data = GsonUtils.getObject(root, "data");
        final JsonObject object = GsonUtils.getObject(data, "object");
        return GsonUtils.getString(object, "id");
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

    public static record CustomerPayload(String email, String name, String customerId) {}
}
package niwer.photon.web.endpoints.stripe;

import com.google.gson.JsonObject;

import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.util.GsonUtils;
import niwer.photon.web.api.stripe.StripeGetCustomerRequest;
import niwer.photon.web.api.stripe.StripeGetInvoiceByIdRequest;
import niwer.photon.web.endpoints.EndpointUtils;

public final class StripeSupport {

    private StripeSupport() {}

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
            final JsonObject latestInvoice = new StripeGetInvoiceByIdRequest(latestInvoiceId).request();
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

        final JsonObject customer = new StripeGetCustomerRequest(customerId).request();
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

    protected static SubscriptionStatus stripeStatusToLocal(String stripeStatus) {
        if (stripeStatus == null || stripeStatus.isBlank()) return SubscriptionStatus.EXPIRED;

        final String normalized = stripeStatus.toLowerCase();
        if (normalized.equals("active") || normalized.equals("trialing") || normalized.equals("past_due")) return SubscriptionStatus.ACTIVE;

        return SubscriptionStatus.EXPIRED;
    }
    
    public static record CustomerPayload(String email, String name, String customerId) {}
}
package niwer.photon.web.endpoints.stripe;

import java.util.Date;

import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import io.javalin.http.Context;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPackProduct;
import niwer.photon.objects.ObjectPurchaseToken;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.sql.PackOwnershipTable;
import niwer.photon.sql.PackProductTable;
import niwer.photon.sql.PurchaseTokenTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.endpoints.IEndpoint;

public class StripeWebhookEndpoint implements IEndpoint {

    @Override public String path() { return "/stripe/webhook"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final String payload = handler.body();
        final String sigHeader = handler.header("Stripe-Signature");
        final String endpointSecret = Directories.getConfig().stripe_webhook_signature;
        final String apiKey = Directories.getConfig().stripe_api_key;

        /* Stripe API */
        if (apiKey == null || apiKey.isBlank()) {
            handler.status(500).result("stripe_api_key is not configured");
            return;
        }
        Stripe.apiKey = apiKey;

        Event event;
        try {
            if (endpointSecret != null && !endpointSecret.isBlank()) {
                event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            } else {
                // No signing secret configured — parse without verification
                event = Event.GSON.fromJson(payload, Event.class);
            }
        } catch (SignatureVerificationException e) {
            handler.status(400).result("Invalid signature");
            return;
        } catch (Exception e) {
            handler.status(400).result("Invalid payload");
            return;
        }

        final String type = event.getType();
        try {
            if (type == null || type.isBlank()) {
                ignore(handler, "missing event type", true);
                return;
            }

            if (type.startsWith("customer.subscription")) {
                handleSubscriptionEvent(handler, payload, apiKey, type);
                return;
            }

            if (type.startsWith("checkout.session.")) {
                handleCheckoutSessionEvent(handler, payload, apiKey, type);
                return;
            }

            if (type.startsWith("invoice.")) {
                handleInvoiceEvent(handler, payload, apiKey, type);
                return;
            }
        } catch (Exception e) {
            Console.log("Error handling Stripe webhook event: " + e.getMessage()).type(PhotonLogTypes.NETWORK).error().container(PhotonEngine.LOGGER).send();
            handler.status(500).result("error");
            return;
        }

        Console.log("Stripe webhook ignored: unhandled event type " + type).type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
        handler.status(200).result("ignored");
    }

    private static void handleSubscriptionEvent(Context handler, String payload, String apiKey, String type) {
        final JsonObject subscription = StripeSupport.resolveSubscription(apiKey, payload);
        if (subscription == null) {
            ignore(handler, "missing subscription object for event " + type, true);
            return;
        }

        final StripeSupport.CustomerPayload customerPayload = StripeSupport.resolveCustomerFromSubscription(apiKey, subscription);
        final String subscriptionId = StripeSupport.getString(subscription, "id");
        if (customerPayload.email() == null || customerPayload.email().isBlank()) {
            ignore(handler, "missing customer email for subscription " + subscriptionId, false);
            return;
        }

        final long periodEnd = StripeSupport.getLong(subscription, "current_period_end") * 1000L;
        final ObjectSubscription subscriptionRecord = SubscriptionTable.upsertSubscription(
            customerPayload.email(),
            customerPayload.name(),
            customerPayload.customerId(),
            subscriptionId,
            StripeSupport.stripeStatusToLocal(StripeSupport.getString(subscription, "status")),
            periodEnd == 0L ? null : new Date(periodEnd)
        );

        handler.status(200).json(subscriptionRecord);
    }

    private static void handleCheckoutSessionEvent(Context handler, String payload, String apiKey, String type) {
        final JsonObject checkoutSession = StripeSupport.resolveCheckoutSession(apiKey, payload);
        if (checkoutSession == null) {
            ignore(handler, "missing checkout session object for event " + type, true);
            return;
        }

        final JsonObject metadata = StripeSupport.getObject(checkoutSession, "metadata");
        final String purchaseToken = StripeSupport.firstNonBlank(
            StripeSupport.getString(checkoutSession, "client_reference_id"),
            StripeSupport.getString(metadata, "purchase_token"),
            StripeSupport.getString(metadata, "purchaseToken"),
            StripeSupport.getString(checkoutSession, "id")
        );
        if (purchaseToken == null || purchaseToken.isBlank()) {
            ignore(handler, "missing purchase token for checkout session " + StripeSupport.getString(checkoutSession, "id"), false);
            return;
        }

        final JsonObject customerDetails = StripeSupport.getObject(checkoutSession, "customer_details");
        final JsonObject subscription = StripeSupport.resolveSubscriptionById(apiKey, StripeSupport.getString(checkoutSession, "subscription"));
        final String productType = StripeSupport.firstNonBlank(
            StripeSupport.getString(metadata, "type"),
            StripeSupport.getString(metadata, "content_type"),
            StripeSupport.getString(checkoutSession, "mode")
        );
        final String packId = StripeSupport.firstNonBlank(
            StripeSupport.getString(metadata, "content_pack_metadata_id"),
            StripeSupport.getString(metadata, "pack_id"),
            StripeSupport.getString(metadata, "contentPackMetadataId")
        );
        final String priceId = StripeSupport.firstNonBlank(StripeSupport.getString(checkoutSession, "price_id"), StripeSupport.getString(checkoutSession, "price"));
        final ObjectPackProduct pack = packId == null || packId.isBlank() ? (priceId == null || priceId.isBlank() ? null : PackProductTable.getByStripePriceId(priceId)) : PackProductTable.getById(packId);
        final StripeSupport.CustomerPayload customerPayload = StripeSupport.resolveCustomer(
            apiKey,
            StripeSupport.getString(checkoutSession, "customer"),
            metadata,
            StripeSupport.firstNonBlank(StripeSupport.getString(customerDetails, "email"), StripeSupport.getString(checkoutSession, "customer_email"))
        );
        final String subscriptionId = StripeSupport.getString(subscription, "id");
        final long periodEnd = subscription == null ? 0L : StripeSupport.getLong(subscription, "current_period_end") * 1000L;
        final SubscriptionStatus status = StripeSupport.stripeStatusToLocal(subscription == null ? StripeSupport.getString(checkoutSession, "status") : StripeSupport.getString(subscription, "status"));

        if (pack != null || "one-time-pack".equalsIgnoreCase(productType)) {
            final ObjectPurchaseToken purchase = PurchaseTokenTable.completePurchase(
                purchaseToken,
                StripeSupport.getString(checkoutSession, "id"),
                StripeSupport.getString(checkoutSession, "customer"),
                null,
                customerPayload.email(),
                customerPayload.name(),
                "ACTIVE",
                null,
                priceId
            );
            if (purchase == null) {
                ignore(handler, "missing pending pack purchase for token " + purchaseToken, false);
                return;
            }

            final String resolvedPackId = pack != null ? pack.id() : packId;
            PackOwnershipTable.upsertOwnership(purchase.customerEmail(), purchase.linkedAccountUuid(), resolvedPackId, true);
            handler.status(200).json(purchase);
            return;
        }

        final var purchase = PurchaseTokenTable.completePurchase(
            purchaseToken,
            StripeSupport.getString(checkoutSession, "id"),
            StripeSupport.getString(checkoutSession, "customer"),
            subscriptionId,
            customerPayload.email(),
            customerPayload.name(),
            status.name(),
            periodEnd == 0L ? null : new java.util.Date(periodEnd)
        );

        if (purchase == null) {
            ignore(handler, "missing pending purchase for token " + purchaseToken, false);
            return;
        }

        final ObjectSubscription subscriptionRecord = SubscriptionTable.upsertSubscription(
            purchase.customerEmail(),
            purchase.customerName(),
            purchase.stripeCustomerId(),
            subscriptionId,
            status,
            periodEnd == 0L ? null : new java.util.Date(periodEnd)
        );

        handler.status(200).json(subscriptionRecord);
    }

    private static void handleInvoiceEvent(Context handler, String payload, String apiKey, String type) {
        final JsonObject invoice = StripeSupport.resolveInvoice(apiKey, payload);
        if (invoice == null) {
            ignore(handler, "missing invoice object for event " + type, true);
            return;
        }

        final StripeSupport.CustomerPayload customerPayload = StripeSupport.resolveCustomer(
            apiKey,
            StripeSupport.getString(invoice, "customer"),
            StripeSupport.getObject(invoice, "metadata"),
            StripeSupport.getString(invoice, "customer_email")
        );
        final String subscriptionId = StripeSupport.getString(invoice, "subscription");
        if (customerPayload.email() == null || customerPayload.email().isBlank()) {
            ignore(handler, "missing customer email for invoice event " + type, false);
            return;
        }

        final long periodEnd = StripeSupport.getLong(invoice, "period_end") * 1000L;
        final SubscriptionStatus status = "invoice.payment_failed".equals(type) ? SubscriptionStatus.EXPIRED : SubscriptionStatus.ACTIVE;
        final ObjectSubscription subscriptionRecord = SubscriptionTable.upsertSubscription(
            customerPayload.email(),
            customerPayload.name(),
            customerPayload.customerId(),
            subscriptionId,
            status,
            periodEnd == 0L ? null : new Date(periodEnd)
        );

        handler.status(200).json(subscriptionRecord);
    }

    private static void ignore(Context handler, String reason, boolean plainIgnoredResponse) {
        Console.log("Stripe webhook ignored: " + reason).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
        handler.status(200).result(plainIgnoredResponse ? "ignored" : "missing customer email");
    }
}

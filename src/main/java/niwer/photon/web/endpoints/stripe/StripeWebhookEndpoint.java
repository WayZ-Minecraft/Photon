package niwer.photon.web.endpoints.stripe;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import io.javalin.http.Context;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.EndpointUtils;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * Handle Stripe webhook events :
 * - checkout.session.completed
 * - customer.subscription.created
 * - customer.subscription.deleted
 * - customer.subscription.paused
 * - customer.subscription.resumed
 * - customer.subscription.updated
 * - invoice.paid
 * - invoice.payment_failed
 */
public class StripeWebhookEndpoint implements IEndpoint {

    @Override public String path() { return "/stripe/webhook"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final String payload = handler.body();
        final String sigHeader = handler.header("Stripe-Signature");
        final String endpointSecret = Directories.getConfig().stripe_webhook_signature;
        final String apiKey = Directories.getConfig().stripe_api_key;

        /* Print a small message so we know we've received the event */
        Console.log("Received Stripe webhook event").type(PhotonLogTypes.STRIPE).container(PhotonEngine.LOGGER).send();

        /* Stripe API */
        if (apiKey == null || apiKey.isBlank()) {
            Console.log("stripe_api_key is not configured").type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            handler.status(500).result("stripe_api_key is not configured");
            return;
        }
        Stripe.apiKey = apiKey;

        Event event;
        try {
            if (endpointSecret != null && !endpointSecret.isBlank()) event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            else event = Event.GSON.fromJson(payload, Event.class); // No signing secret configured — parse without verification
        } catch (SignatureVerificationException e) {
            Console.log("Invalid Stripe webhook signature: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            handler.status(400).result("Invalid signature");
            return;
        } catch (Exception e) {
            Console.log("Invalid Stripe webhook payload: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
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
        final String subscriptionId = GsonUtils.getString(subscription, "id");
        if (customerPayload.email() == null || customerPayload.email().isBlank()) {
            ignore(handler, "missing customer email for subscription " + subscriptionId, false);
            return;
        }

        final long periodEnd = GsonUtils.getLong(subscription, "current_period_end") * 1000L;
        final String stripeStatus = GsonUtils.getString(subscription, "status");
        final SubscriptionStatus status = StripeSupport.stripeStatusToLocal(stripeStatus);

        final ObjectSubscription subscriptionRecord = SubscriptionTable.upsertSubscription(
            customerPayload.email(),
            customerPayload.name(),
            customerPayload.customerId(),
            subscriptionId,
            status,
            periodEnd == 0L ? null : new Date(periodEnd)
        );

        // If subscription is canceled or deleted, revoke GitHub access
        if ("customer.subscription.deleted".equals(type) || status == SubscriptionStatus.CANCELED || status == SubscriptionStatus.EXPIRED) {
            revokeGitHubAccessForSubscription(subscriptionRecord);
        }

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
            GsonUtils.getString(invoice, "customer"),
            GsonUtils.getObject(invoice, "metadata"),
            GsonUtils.getString(invoice, "customer_email")
        );
        final String subscriptionId = GsonUtils.getString(invoice, "subscription");
        if (customerPayload.email() == null || customerPayload.email().isBlank()) {
            ignore(handler, "missing customer email for invoice event " + type, false);
            return;
        }

        final long periodEnd = GsonUtils.getLong(invoice, "period_end") * 1000L;
        final SubscriptionStatus status = "invoice.payment_failed".equals(type) ? SubscriptionStatus.EXPIRED : SubscriptionStatus.ACTIVE;
        final ObjectSubscription subscriptionRecord = SubscriptionTable.upsertSubscription(
            customerPayload.email(),
            customerPayload.name(),
            customerPayload.customerId(),
            subscriptionId,
            status,
            periodEnd == 0L ? null : new Date(periodEnd)
        );

        // Revoke GitHub access if payment failed
        if ("invoice.payment_failed".equals(type)) {
            revokeGitHubAccessForSubscription(subscriptionRecord);
        }

        handler.status(200).json(subscriptionRecord);
    }

    /**
     * Helper to look up the user account and revoke GitHub permissions asynchronously
     */
    private static void revokeGitHubAccessForSubscription(ObjectSubscription subscription) {
        if (subscription == null || subscription.accountUuid() == null) return;

        Thread.ofVirtual().start(() -> {
            try {
                // Fetch the user account via UUID to get their GitHub username
                final ObjectUserAccount account = PlayerAccountTable.getAccountByUUID(subscription.accountUuid());
                if (account == null || account.getUsername() == null || account.getUsername().isBlank()) return;

                final String githubUser = account.getUsername();

                // Remove repo collaborator access
                new niwer.photon.web.api.github.RemoveRepositoryCollaboratorRequest(githubUser).request();

                // Remove from the customer team
                new niwer.photon.web.api.github.RemoveTeamMemberRequest(githubUser).request();

                Console.log("Revoked GitHub access for " + githubUser).type(PhotonLogTypes.STRIPE).container(PhotonEngine.LOGGER).send();
            } catch (Exception e) {
                Console.log("Error revoking GitHub access: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            }
        });
    }

    private static void handleCheckoutSessionEvent(Context handler, String payload, String apiKey, String type) {
        final JsonObject checkoutSession = StripeSupport.resolveCheckoutSession(apiKey, payload);
        if (checkoutSession == null) {
            ignore(handler, "missing checkout session object for event " + type, true);
            return;
        }

        final JsonObject metadata = GsonUtils.getObject(checkoutSession, "metadata");
        final String purchaseToken = EndpointUtils.firstNonBlank(
            GsonUtils.getString(checkoutSession, "client_reference_id"),
            GsonUtils.getString(metadata, "purchase_token"),
            GsonUtils.getString(metadata, "purchaseToken"),
            GsonUtils.getString(checkoutSession, "id")
        );
        if (purchaseToken == null || purchaseToken.isBlank()) {
            ignore(handler, "missing purchase token for checkout session " + GsonUtils.getString(checkoutSession, "id"), false);
            return;
        }

        // 1. Extract GitHub username from Custom Fields or Metadata fallback
        String githubUsername = extractCustomField(checkoutSession, "github_username");
        if (githubUsername == null || githubUsername.isBlank()) {
            githubUsername = GsonUtils.getString(metadata, "github_username");
        }

        final JsonObject customerDetails = GsonUtils.getObject(checkoutSession, "customer_details");
        final JsonObject subscription = StripeSupport.resolveSubscriptionById(apiKey, GsonUtils.getString(checkoutSession, "subscription"));
        final StripeSupport.CustomerPayload customerPayload = StripeSupport.resolveCustomer(
            apiKey,
            GsonUtils.getString(checkoutSession, "customer"),
            metadata,
            EndpointUtils.firstNonBlank(GsonUtils.getString(customerDetails, "email"), GsonUtils.getString(checkoutSession, "customer_email"))
        );
        final String subscriptionId = GsonUtils.getString(subscription, "id");
        final long periodEnd = subscription == null ? 0L : GsonUtils.getLong(subscription, "current_period_end") * 1000L;
        final SubscriptionStatus status = StripeSupport.stripeStatusToLocal(subscription == null ? GsonUtils.getString(checkoutSession, "status") : GsonUtils.getString(subscription, "status"));

        final var purchase = PurchaseTable.completePurchase(
            purchaseToken,
            GsonUtils.getString(checkoutSession, "id"),
            GsonUtils.getString(checkoutSession, "customer"),
            subscriptionId,
            customerPayload.email(),
            customerPayload.name(),
            status,
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

        // 2. Trigger GitHub Repo Creation & Team Addition Asynchronously
        if (githubUsername != null && !githubUsername.isBlank()) {
            final String finalGhUser = githubUsername.trim();
            Thread.ofVirtual().start(() -> {
                try {
                    Console.log("Starting GitHub provisioning for: " + finalGhUser)
                        .type(PhotonLogTypes.STRIPE)
                        .container(PhotonEngine.LOGGER)
                        .send();

                    // Step A: Create the template repository
                    new niwer.photon.web.api.github.CreateRepositoryRequest(finalGhUser).request();

                    // Brief pause so GitHub finishes initializing the new repository
                    Thread.sleep(2500);

                    // Step B: Grant customer permissions (invite as collaborator)
                    new niwer.photon.web.api.github.SetRepositoryPermissionsRequest(finalGhUser, "admin").request();

                    // Step C: Add customer to organization team
                    new niwer.photon.web.api.github.AddTeamMemberRequest(finalGhUser).request();

                    Console.log("GitHub provisioning completed for: " + finalGhUser)
                        .type(PhotonLogTypes.STRIPE)
                        .container(PhotonEngine.LOGGER)
                        .send();
                } catch (Exception e) {
                    Console.log("Failed GitHub provisioning for " + finalGhUser + ": " + e.getMessage())
                        .type(PhotonLogTypes.STRIPE)
                        .error()
                        .container(PhotonEngine.LOGGER)
                        .send();
                }
            });
        }

        handler.status(200).json(subscriptionRecord);
    }

    private static void ignore(Context handler, String reason, boolean plainIgnoredResponse) {
        Console.log("Stripe webhook ignored: " + reason).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
        handler.status(200).result(plainIgnoredResponse ? "ignored" : "missing customer email");
    }
    
    public static String extractCustomField(JsonObject checkoutSession, String fieldKey) {
        if (checkoutSession == null || !checkoutSession.has("custom_fields")) return null;
        
        try {
            JsonArray customFields = checkoutSession.getAsJsonArray("custom_fields");
            if (customFields == null) return null;

            for (JsonElement el : customFields) {
                JsonObject field = el.getAsJsonObject();
                String key = GsonUtils.getString(field, "key");
                
                // Check if key matches (or falls back to text label)
                if (fieldKey.equalsIgnoreCase(key)) {
                    JsonObject textObj = field.getAsJsonObject("text");
                    if (textObj != null && textObj.has("value")) {
                        return textObj.get("value").getAsString().trim();
                    }
                }
            }
        } catch (Exception e) {
            // Silently catch if parsing custom_fields fails
        }
        return null;
    }
}

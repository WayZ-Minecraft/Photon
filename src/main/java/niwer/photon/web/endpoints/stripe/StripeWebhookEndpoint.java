package niwer.photon.web.endpoints.stripe;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import io.javalin.http.Context;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPurchase;
import niwer.photon.objects.stripe.StripeCheckoutSession;
import niwer.photon.objects.stripe.StripeCustomer;
import niwer.photon.objects.stripe.StripeInvoice;
import niwer.photon.objects.stripe.StripeSubscription;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.api.github.AddTeamMemberRequest;
import niwer.photon.web.api.github.CreateRepositoryRequest;
import niwer.photon.web.api.github.RemoveRepositoryCollaboratorRequest;
import niwer.photon.web.api.github.RemoveTeamMemberRequest;
import niwer.photon.web.api.github.SetRepositoryPermissionsRequest;
import niwer.photon.web.api.stripe.StripeGetCheckoutSessionByIdRequest;
import niwer.photon.web.api.stripe.StripeGetCustomerRequest;
import niwer.photon.web.api.stripe.StripeGetInvoiceByIdRequest;
import niwer.photon.web.api.stripe.StripeGetSubByIdRequest;
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
        final String PAYLOAD = handler.body();
        final String SIG_HEADER = handler.header("Stripe-Signature");
        final String ENDPOINT_SECRET = Directories.getConfig().stripe_webhook_signature;
        final String API_KEY = Directories.getConfig().stripe_api_key;

        /* Stripe API */
        if (API_KEY == null || API_KEY.isBlank()) {
            Console.log("stripe_api_key is not configured").type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            handler.status(500).result("stripe_api_key is not configured");
            return;
        }
        Stripe.apiKey = API_KEY;

        /* Parse the webhook event */
        final Event EVENT;
        try {
            EVENT = ENDPOINT_SECRET != null && !ENDPOINT_SECRET.isBlank() ? Webhook.constructEvent(PAYLOAD, SIG_HEADER, ENDPOINT_SECRET) : Event.GSON.fromJson(PAYLOAD, Event.class); // No signing secret configured — parse without verification
        } catch (SignatureVerificationException e) {
            Console.log("Invalid Stripe webhook signature: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            handler.status(400).result("Invalid signature");
            return;
        } catch (Exception e) {
            Console.log("Invalid Stripe webhook payload: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            handler.status(400).result("Invalid payload");
            return;
        }

        /* Determine the event type */
        final String EVENT_TYPE = EVENT.getType();
        try {
            /* Print a small message so we know we've received the event */
            Console.log("Received Stripe webhook event : " + EVENT_TYPE).type(PhotonLogTypes.STRIPE).container(PhotonEngine.LOGGER).send();

            /* Check if the event type is valid */
            if (EVENT_TYPE == null || EVENT_TYPE.isBlank()) {
                ignore(handler, "missing event type", true);
                return;
            }

            /* Handle subscription events */
            if (EVENT_TYPE.startsWith("customer.subscription")) {
                handleSubscriptionEvent(handler, PAYLOAD, EVENT_TYPE);
                return;
            }

            /* Handle checkout session events */
            if (EVENT_TYPE.startsWith("checkout.session.")) {
                handleCheckoutSessionEvent(handler, PAYLOAD, EVENT_TYPE);
                return;
            }

            /* Handle invoice events */
            if (EVENT_TYPE.startsWith("invoice.")) {
                handleInvoiceEvent(handler, PAYLOAD, EVENT_TYPE);
                return;
            }
        } catch (Exception e) {
            Console.log("Error handling Stripe webhook event: " + e.getMessage()).type(PhotonLogTypes.NETWORK).error().container(PhotonEngine.LOGGER).send();
            handler.status(500).result("error");
            return;
        }

        Console.log("Stripe webhook ignored: unhandled event type " + EVENT_TYPE).type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
        handler.status(200).result("ignored");
    }

    private static void handleSubscriptionEvent(Context handler, String payload, String eventType) {
        final StripeSubscription SUBSCRIPTION = new StripeGetSubByIdRequest(payload, true).request();
        if (SUBSCRIPTION == null) {
            ignore(handler, "missing subscription object for event " + eventType, true);
            return;
        }

        /* Resolve customer from subscription */
        final StripeCustomer CUSTOMER = StripeGetCustomerRequest.resolveCustomerFromSubscription(SUBSCRIPTION);
        if (CUSTOMER.email() == null || CUSTOMER.email().isBlank()) {
            ignore(handler, "missing customer email for subscription " + SUBSCRIPTION.id(), false);
            return;
        }

        /* If subscription is canceled or deleted, revoke GitHub access */
        if ("customer.subscription.deleted".equals(eventType) || SUBSCRIPTION.status() == SubscriptionStatus.CANCELED || SUBSCRIPTION.status() == SubscriptionStatus.EXPIRED) revokeGitHubAccess(CUSTOMER.id());

        handler.status(200).json(SubscriptionTable.upsertSubscription(CUSTOMER.email(), CUSTOMER.name(), CUSTOMER.id(), SUBSCRIPTION.id(), SUBSCRIPTION.status(), null));
    }

    private static void handleInvoiceEvent(Context handler, String payload, String eventType) {
        final StripeInvoice INVOICE = new StripeGetInvoiceByIdRequest(payload, true).request();
        if (INVOICE == null) {
            ignore(handler, "missing invoice object for event " + eventType, true);
            return;
        }

        final StripeCustomer CUSTOMER = StripeGetCustomerRequest.resolveCustomerFromInvoice(INVOICE);
        if (CUSTOMER.email() == null || CUSTOMER.email().isBlank()) {
            ignore(handler, "missing customer email for invoice event " + eventType, false);
            return;
        }

        /* Revoke GitHub access if payment failed */
        if ("invoice.payment_failed".equals(eventType)) revokeGitHubAccess(INVOICE.customerId());

        final SubscriptionStatus STATUS = "invoice.payment_failed".equals(eventType) ? SubscriptionStatus.EXPIRED : SubscriptionStatus.ACTIVE;
        handler.status(200).json(SubscriptionTable.upsertSubscription(CUSTOMER.email(), CUSTOMER.name(), CUSTOMER.id(), null, STATUS, null));
    }

    private static void handleCheckoutSessionEvent(Context handler, String payload, String eventType) {
        final StripeCheckoutSession CHECKOUT_SESSION = new StripeGetCheckoutSessionByIdRequest(payload, true).request();
        if (CHECKOUT_SESSION == null) {
            ignore(handler, "missing checkout session object for event " + eventType, true);
            return;
        }

        /* Resolve the purchase token */
        final String PURCHASE_TOKEN = EndpointUtils.firstNonBlank(CHECKOUT_SESSION.clientRefId(), CHECKOUT_SESSION.id());
        if (PURCHASE_TOKEN == null || PURCHASE_TOKEN.isBlank()) {
            ignore(handler, "missing purchase token for checkout session " + CHECKOUT_SESSION.id(), false);
            return;
        }

        /* Resolve the GitHub username */
        final String GITHUB_USERNAME = CHECKOUT_SESSION.getCustomFieldByKeys("githubusername").text().value().trim();

        /* Resolve the subscription */
        final StripeSubscription SUBSCRIPTION = new StripeGetSubByIdRequest(CHECKOUT_SESSION.subscriptionId()).request();
        final var PURCHASE = PurchaseTable.completePurchase(PURCHASE_TOKEN, CHECKOUT_SESSION.id(), CHECKOUT_SESSION.customerID(), CHECKOUT_SESSION.subscriptionId(), CHECKOUT_SESSION.customerDetails().email(), CHECKOUT_SESSION.customerDetails().name(), SUBSCRIPTION.status(), null, GITHUB_USERNAME);
        if (PURCHASE == null) {
            ignore(handler, "missing pending purchase for token " + PURCHASE_TOKEN, false);
            return;
        }

        /* Create the repo and/or invite/set permissions, finally add the user to the team, if a GitHub username is provided */
        if (GITHUB_USERNAME != null && !GITHUB_USERNAME.isBlank()) {
            Thread.ofVirtual().start(() -> {
                try {
                    new CreateRepositoryRequest(GITHUB_USERNAME).request(); // Step A: Create the template repository
                    Thread.sleep(2500); // Brief pause so GitHub finishes initializing the new repository
                    new SetRepositoryPermissionsRequest(GITHUB_USERNAME, "admin").request(); // Step B: Grant customer permissions (invite as collaborator)
                    new AddTeamMemberRequest(GITHUB_USERNAME).request(); // Add customer to organization team
                } catch (Exception e) {
                    Console.log("Failed GitHub provisioning for " + GITHUB_USERNAME + ": " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
                }
            });
        }

        handler.status(200).json(SubscriptionTable.upsertSubscription(PURCHASE.customerEmail(), PURCHASE.customerName(), PURCHASE.stripeCustomerId(), SUBSCRIPTION.id(), SUBSCRIPTION.status(), null));
    }

    private static void revokeGitHubAccess(String stripeCustomerId) {
        if (stripeCustomerId == null) return;

        Thread.ofVirtual().start(() -> {
            try {
                final ObjectPurchase PURCHASE = PurchaseTable.getByCustomerId(stripeCustomerId);
                if (PURCHASE == null) return;

                final String GITHUB_USERNAME = PURCHASE.githubUsername();
                new RemoveRepositoryCollaboratorRequest(GITHUB_USERNAME).request(); // Remove repo collaborator access
                new RemoveTeamMemberRequest(GITHUB_USERNAME).request(); // Remove from the customer team
            } catch (Exception e) {
                Console.log("Error revoking GitHub access: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            }
        });
    }

    private static void ignore(Context handler, String reason, boolean plainIgnoredResponse) {
        Console.log("Stripe webhook ignored: " + reason).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
        handler.status(200).result(plainIgnoredResponse ? "ignored" : "missing customer email");
    }
}

package niwer.photon.web.endpoints.stripe;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import io.javalin.http.Context;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPurchase;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.util.subscribtion.SubscriptionStatus;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.api.github.AddTeamMemberRequest;
import niwer.photon.web.api.github.CreateRepositoryRequest;
import niwer.photon.web.api.github.RemoveRepositoryCollaboratorRequest;
import niwer.photon.web.api.github.RemoveTeamMemberRequest;
import niwer.photon.web.api.github.SetRepositoryPermissionsRequest;
import niwer.photon.web.endpoints.IEndpoint;

public class StripeWebhookEndpoint implements IEndpoint {

    @Override public String path() { return "/stripe/webhook"; }
    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context ctx) {
        final String payload = ctx.body();
        final String sigHeader = ctx.header("Stripe-Signature");
        final String webhookSecret = Directories.getConfig().stripe_webhook_signature;
        final String apiKey = Directories.getConfig().stripe_api_key;

        if (apiKey == null || apiKey.isBlank()) {
            ctx.status(500).result("stripe_api_key is not configured");
            return;
        }
        Stripe.apiKey = apiKey;

        final Event event;
        try {
            if (webhookSecret != null && !webhookSecret.isBlank()) {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } else {
                event = Event.GSON.fromJson(payload, Event.class);
            }
        } catch (SignatureVerificationException e) {
            Console.log("Invalid signature: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            ctx.status(400).result("Invalid signature");
            return;
        } catch (Exception e) {
            ctx.status(400).result("Invalid payload");
            return;
        }

        final EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        final StripeObject stripeObject = deserializer.getObject().orElse(null);

        if (stripeObject == null) {
            ctx.status(200).result("Ignored: schema mismatch");
            return;
        }

        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject);
                case "customer.subscription.created",
                     "customer.subscription.updated" -> handleSubscriptionUpsert((Subscription) stripeObject);
                case "customer.subscription.deleted" -> handleSubscriptionDeleted((Subscription) stripeObject);
                case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject);
                case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject);
                default -> Console.log("Unhandled webhook event: " + event.getType())
                    .type(PhotonLogTypes.STRIPE).container(PhotonEngine.LOGGER).send();
            }
            ctx.status(200).result("ok");
        } catch (Exception e) {
            Console.log("Error processing webhook " + event.getType() + ": " + e.getMessage())
                .type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            ctx.status(500).result("Internal error");
        }
    }

    private void handleCheckoutSessionCompleted(Session session) throws Exception {

    }

    private void handleSubscriptionUpsert(Subscription sub) throws Exception {
    }

    private void handleSubscriptionDeleted(Subscription sub) {
    }

    private void handleInvoicePaid(Invoice invoice) {
    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
    }

    private static SubscriptionStatus mapSubscriptionStatus(String stripeStatus) {
        if (stripeStatus == null) return SubscriptionStatus.EXPIRED;
        return switch (stripeStatus) {
            case "active", "trialing" -> SubscriptionStatus.ACTIVE;
            case "past_due", "unpaid" -> SubscriptionStatus.PENDING;
            case "canceled" -> SubscriptionStatus.CANCELED;
            default -> SubscriptionStatus.EXPIRED;
        };
    }

    private static void provisionGitHubAsync(String githubUsername) {
        Thread.ofVirtual().start(() -> {
            try {
                new CreateRepositoryRequest(githubUsername).request();
                Thread.sleep(2500);
                new SetRepositoryPermissionsRequest(githubUsername, "admin").request();
                new AddTeamMemberRequest(githubUsername).request();
            } catch (Exception e) {
                Console.log("Failed GitHub provisioning for " + githubUsername + ": " + e.getMessage())
                    .type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            }
        });
    }

    private static void revokeGitHubAccess(String stripeCustomerId) {
        if (stripeCustomerId == null) return;
        Thread.ofVirtual().start(() -> {
            try {
                final ObjectPurchase purchase = PurchaseTable.getByCustomerId(stripeCustomerId);
                if (purchase == null || purchase.githubUsername() == null) return;
                new RemoveRepositoryCollaboratorRequest(purchase.githubUsername()).request();
                new RemoveTeamMemberRequest(purchase.githubUsername()).request();
            } catch (Exception e) {
                Console.log("Error revoking GitHub access: " + e.getMessage())
                    .type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            }
        });
    }
}
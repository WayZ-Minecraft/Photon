package niwer.photon.web.endpoints.stripe;

import java.util.Date;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Price;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionListLineItemsParams;

import io.javalin.http.Context;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPurchase;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.util.stripe.StripeHelper;
import niwer.photon.util.stripe.StripePurchaseStatus;
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
                case "customer.subscription.created", "customer.subscription.updated" -> handleSubscriptionUpsert((Subscription) stripeObject);
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
        final var lineItems = session.listLineItems(SessionListLineItemsParams.builder().setLimit(1L).build());
        final Price price = (lineItems != null && !lineItems.getData().isEmpty()) ? lineItems.getData().get(0).getPrice() : null;
        final String productId = StripeHelper.resolveProductId(price);

        final String customerId = session.getCustomer();
        final Customer customer = customerId != null ? Customer.retrieve(customerId) : null;
        final String email = StripeHelper.resolveEmail(customer, session.getCustomerDetails());
        final String name = StripeHelper.resolveName(customer, session.getCustomerDetails());
        final String refToken = session.getClientReferenceId() != null && !session.getClientReferenceId().isBlank()
            ? session.getClientReferenceId()
            : session.getId();

        final String githubUsername = session.getMetadata() != null ? session.getMetadata().get("github_username") : null;

        if ("subscription".equalsIgnoreCase(session.getMode())) {
            final String subId = session.getSubscription();
            if (subId != null) {
                final Subscription sub = Subscription.retrieve(subId);
                handleSubscriptionUpsert(sub);
            }
        } else {
            final StripePurchaseStatus status = "paid".equalsIgnoreCase(session.getPaymentStatus())
                ? StripePurchaseStatus.ACTIVE
                : StripePurchaseStatus.PENDING;

            PurchaseTable.upsertCompletedPurchase(
                refToken, session.getId(), customerId, null, email, name, status, null, githubUsername, productId
            );

            if (status == StripePurchaseStatus.ACTIVE && githubUsername != null && !githubUsername.isBlank()) {
                provisionGitHubAsync(githubUsername);
            }
        }
    }

    private void handleSubscriptionUpsert(Subscription sub) throws Exception {
        final Customer customer = sub.getCustomer() != null ? Customer.retrieve(sub.getCustomer()) : null;
        final String rawEmail = customer != null ? customer.getEmail() : null;
        final String email = SubscriptionTable.normalizeEmail(rawEmail);
        if (email == null || email.isBlank()) return;

        final Price price = StripeHelper.extractPrice(sub);
        final String productId = StripeHelper.resolveProductId(price);
        final StripePurchaseStatus status = StripeHelper.mapSubscriptionStatus(sub.getStatus());
        final Date expiresAt = StripeHelper.resolveSubscriptionExpiry(sub);

        // Preserve already linked account UUID if exists
        final ObjectSubscription existing = SubscriptionTable.getBySubscriptionId(sub.getId());
        final String accountUuid = existing != null ? existing.accountUuid() : null;

        SubscriptionTable.upsertSubscription(
            email,
            customer != null ? customer.getName() : null,
            sub.getCustomer(),
            sub.getId(),
            status,
            expiresAt,
            accountUuid,
            productId
        );

        final String githubUsername = sub.getMetadata() != null ? sub.getMetadata().get("github_username") : null;
        if (status == StripePurchaseStatus.ACTIVE && githubUsername != null && !githubUsername.isBlank()) {
            provisionGitHubAsync(githubUsername);
        } else if (status != StripePurchaseStatus.ACTIVE) {
            revokeGitHubAccess(sub.getCustomer());
        }
    }

    private void handleSubscriptionDeleted(Subscription sub) {
        final ObjectSubscription existing = SubscriptionTable.getBySubscriptionId(sub.getId());
        if (existing != null) {
            SubscriptionTable.upsertSubscription(
                existing.customerEmail(),
                existing.customerName(),
                existing.customerId(),
                existing.subscriptionId(),
                StripePurchaseStatus.CANCELED,
                new Date(),
                existing.accountUuid(),
                existing.productId()
            );
        }
        revokeGitHubAccess(sub.getCustomer());
    }

    private void handleInvoicePaid(Invoice invoice) throws Exception {
        // final String subId = invoice.getSubscription();
        // if (subId != null && !subId.isBlank()) {
        //     final Subscription sub = Subscription.retrieve(subId);
        //     handleSubscriptionUpsert(sub);
        // }
    }

    private void handleInvoicePaymentFailed(Invoice invoice) throws Exception {
        // final String subId = invoice.getSubscription();
        // if (subId != null && !subId.isBlank()) {
        //     final Subscription sub = Subscription.retrieve(subId);
        //     handleSubscriptionUpsert(sub);
        // }
    }

    private static void provisionGitHubAsync(String githubUsername) {
        Thread.ofVirtual().start(() -> {
            try {
                new CreateRepositoryRequest(githubUsername).request();
                Thread.sleep(2500);
                new SetRepositoryPermissionsRequest(githubUsername, "admin").request();
                new AddTeamMemberRequest(githubUsername).request();
            } catch (Exception e) {
                Console.log("Failed GitHub provisioning for " + githubUsername + ": " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            }
        });
    }

    private static void revokeGitHubAccess(String stripeCustomerId) {
        if (stripeCustomerId == null) return;
        Thread.ofVirtual().start(() -> {
            try {
                final ObjectPurchase purchase = PurchaseTable.getByCustomerId(stripeCustomerId);
                if (purchase != null && purchase.githubUsername() != null) {
                    new RemoveRepositoryCollaboratorRequest(purchase.githubUsername()).request();
                    new RemoveTeamMemberRequest(purchase.githubUsername()).request();
                }
            } catch (Exception e) {
                Console.log("Error revoking GitHub access: " + e.getMessage())
                    .type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            }
        });
    }
}
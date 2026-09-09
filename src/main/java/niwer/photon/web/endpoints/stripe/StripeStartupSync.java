package niwer.photon.web.endpoints.stripe;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionListParams;

import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.util.subscribtion.SubscriptionStatus;

public final class StripeStartupSync {

    private StripeStartupSync() {}

    public static void load() {
        final String API_KEY = Directories.getConfig().stripe_api_key;
        if (API_KEY == null || API_KEY.isBlank()) {
            Console.log("Stripe startup sync aborted: stripe_api_key missing").type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            return;
        }
        Stripe.apiKey = API_KEY;

        /* Synchronize Stripe data on startup */
        int subsSeen = 0, subsUpserted = 0, purchasesSeen = 0, purchasesUpserted = 0;
        try {
            final var SUB_PARAMS = SubscriptionListParams.builder().setStatus(SubscriptionListParams.Status.ALL).setLimit(100L).addExpand("data.customer").build();
            final Set<String> SEE_SUB_EMAILS = new HashSet<>(), SEEN_SESSIONS = new HashSet<>();
            for (final Subscription SUB : Subscription.list(SUB_PARAMS).autoPagingIterable(SUB_PARAMS.toMap())) {
                subsSeen++;
                final Customer CUSTOMER = SUB.getCustomerObject();
                if (CUSTOMER == null || CUSTOMER.getEmail() == null || CUSTOMER.getEmail().isBlank() || !SEE_SUB_EMAILS.add(CUSTOMER.getEmail())) continue;

                final String EMAIL = SubscriptionTable.normalizeEmail(CUSTOMER.getEmail());
                if (EMAIL == null || EMAIL.isBlank() || !SEE_SUB_EMAILS.add(EMAIL)) continue;

                final Price PRICE = (SUB.getItems() != null && !SUB.getItems().getData().isEmpty()) ? SUB.getItems().getData().get(0).getPrice() : null;
                final Date EXPIRES_AT = SUB.getEndedAt() != null ? new Date(SUB.getEndedAt() * 1000L) : null;

                SubscriptionTable.upsertSubscription(EMAIL, CUSTOMER.getName(), SUB.getCustomer(), SUB.getId(), mapStatus(SUB.getStatus()), EXPIRES_AT, null, resolveProductId(PRICE));
                subsUpserted++;
            }

            final var SESSION_PARAMS = SessionListParams.builder().setLimit(100L).addExpand("data.customer").addExpand("data.line_items").build();
            for (final Session SESSION : Session.list(SESSION_PARAMS).autoPagingIterable(SESSION_PARAMS.toMap())) {
                if ("subscription".equals(SESSION.getMode()) || SESSION.getId() == null || !SEEN_SESSIONS.add(SESSION.getId())) continue;
                purchasesSeen++;

                final Customer CUSTOM = SESSION.getCustomerObject();
                final var DETAILS = SESSION.getCustomerDetails();
                final Price PRICE = (SESSION.getLineItems() != null && !SESSION.getLineItems().getData().isEmpty()) ? SESSION.getLineItems().getData().get(0).getPrice() : null;
                final SubscriptionStatus STATUS = "paid".equalsIgnoreCase(SESSION.getPaymentStatus()) ? SubscriptionStatus.ACTIVE : SubscriptionStatus.PENDING;
                final String REF_ID = SESSION.getClientReferenceId() != null && !SESSION.getClientReferenceId().isBlank() ? SESSION.getClientReferenceId() : SESSION.getId();

                PurchaseTable.upsertCompletedPurchase(REF_ID, SESSION.getId(), CUSTOM != null ? CUSTOM.getId() : SESSION.getCustomer(), null,
                    CUSTOM != null ? CUSTOM.getEmail() : (DETAILS != null ? DETAILS.getEmail() : null),
                    CUSTOM != null ? CUSTOM.getName() : (DETAILS != null ? DETAILS.getName() : null),
                    STATUS, null, null, resolveProductId(PRICE));
                purchasesUpserted++;
            }
        } catch (Exception e) {
            Console.log("Stripe startup sync failed: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            return;
        }

        Console.log(String.format("Stripe startup sync finished: subscriptions[seen=%d, upserted=%d], purchases[seen=%d, upserted=%d]", subsSeen, subsUpserted, purchasesSeen, purchasesUpserted)).type(PhotonLogTypes.STRIPE).container(PhotonEngine.LOGGER).send();
    }

    private static String resolveProductId(Price price) {
        if (price == null) return null;
        final var product = Directories.getConfig().productByStripePriceOrProductId(price.getId(), price.getProduct());
        return product != null ? product.id() : null;
    }

    private static SubscriptionStatus mapStatus(String stripeStatus) {
        if (stripeStatus == null) return SubscriptionStatus.EXPIRED;
        return switch (stripeStatus) {
            case "active", "trialing" -> SubscriptionStatus.ACTIVE;
            case "past_due", "unpaid" -> SubscriptionStatus.PENDING;
            case "canceled" -> SubscriptionStatus.CANCELED;
            default -> SubscriptionStatus.EXPIRED;
        };
    }
}
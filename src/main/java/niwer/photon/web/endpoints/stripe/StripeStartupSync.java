package niwer.photon.web.endpoints.stripe;

import java.util.LinkedHashMap;
import java.util.Map;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.stripe.StripeCustomer;
import niwer.photon.objects.stripe.StripeSubscription;
import niwer.photon.objects.stripe.StripeSubscriptionList;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.api.stripe.StripeGetCustomerRequest;
import niwer.photon.web.api.stripe.StripeListSubsRequests;

public final class StripeStartupSync {

    private static final int PAGE_SIZE = 100;

    private StripeStartupSync() {}

    public static void load() {
        try {
            run();
        } catch (Exception e) {
            Console.log("Stripe startup sync failed: " + e.getMessage()).type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
        }
    }

    private static void run() {
        int seenSubscriptions = 0;
        int upserted = 0;
        int skippedNoEmail = 0;
        int errors = 0;
        
        final Map<String, StripeSubscription> LATEST_BY_EMAIL = new LinkedHashMap<>();
        String startingAfter = null;

        while (true) {
            final StripeSubscriptionList CONTAINER = new StripeListSubsRequests(startingAfter, PAGE_SIZE).request();
            if (CONTAINER == null) break;

            /* Get all subscriptions from the list */
            for (final StripeSubscription SUBSCRIPTION : CONTAINER.data()) {
                if (SUBSCRIPTION == null) continue;
                seenSubscriptions++;

                final StripeCustomer customer = StripeGetCustomerRequest.resolveCustomerFromSubscription(SUBSCRIPTION);
                if (customer.email() == null || customer.email().isBlank()) {
                    skippedNoEmail++;
                    continue;
                }

                final String normalizedEmail = SubscriptionTable.normalizeEmail(customer.email());
                if (normalizedEmail == null || normalizedEmail.isBlank()) {
                    skippedNoEmail++;
                    continue;
                }

                LATEST_BY_EMAIL.putIfAbsent(normalizedEmail, SUBSCRIPTION);
            }

            if (!CONTAINER.hasMore()) break;

            startingAfter = CONTAINER.last().id();
            if (startingAfter == null || startingAfter.isBlank()) break;
        }

        for (StripeSubscription subscription : LATEST_BY_EMAIL.values()) {
            try {
                final StripeCustomer customer = StripeGetCustomerRequest.resolveCustomerFromSubscription(subscription);
                final String email = SubscriptionTable.normalizeEmail(customer.email());
                final String subscriptionId = subscription.id();

                if (email == null || email.isBlank() || subscriptionId == null || subscriptionId.isBlank()) {
                    skippedNoEmail++;
                    continue;
                }

                SubscriptionTable.upsertSubscription(email, customer.name(), customer.id(), subscriptionId, subscription.status(), null );
                upserted++;
            } catch (Exception e) {
                errors++;
            }
        }
        Console.log("Stripe startup sync finished: subscriptions=" + seenSubscriptions + ", upserted=" + upserted + ", skippedNoEmail=" + skippedNoEmail + ", errors=" + errors).type(PhotonLogTypes.STRIPE).container(PhotonEngine.LOGGER).send();
    }
}

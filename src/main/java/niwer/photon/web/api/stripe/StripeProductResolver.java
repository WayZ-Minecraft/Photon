package niwer.photon.web.api.stripe;

import niwer.photon.Directories;
import niwer.photon.objects.stripe.StripeCheckoutLineItems;
import niwer.photon.objects.stripe.StripeSubscription;

public final class StripeProductResolver {

    private StripeProductResolver() {}

    public static String productIdForCheckoutSession(String checkoutSessionId) {
        if (checkoutSessionId == null || checkoutSessionId.isBlank()) return null;
        final StripeCheckoutLineItems lineItems = new StripeGetCheckoutLineItemsRequest(checkoutSessionId).request();
        if (lineItems == null) return null;
        return lineItems.data().stream()
            .filter(item -> Directories.getConfig().productByStripePriceOrProductId(item.priceId(), item.productId()) != null)
            .map(item -> Directories.getConfig().productByStripePriceOrProductId(item.priceId(), item.productId()))
            .filter(product -> product != null)
            .map(product -> product.id())
            .findFirst()
            .orElse(null);
    }

    public static String productIdForSubscription(StripeSubscription subscription) {
        if (subscription == null) return null;
        final var product = Directories.getConfig().productByStripePriceOrProductId(subscription.priceId(), subscription.productId());
        return product == null ? null : product.id();
    }
}
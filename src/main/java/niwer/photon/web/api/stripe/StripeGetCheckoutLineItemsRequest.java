package niwer.photon.web.api.stripe;

import niwer.photon.objects.stripe.StripeCheckoutLineItems;

public class StripeGetCheckoutLineItemsRequest extends StripeApiRequest<StripeCheckoutLineItems> {

    private final String checkoutSessionId;

    public StripeGetCheckoutLineItemsRequest(String checkoutSessionId) {
        super(StripeCheckoutLineItems.class);
        this.checkoutSessionId = this.encode(checkoutSessionId);
    }

    @Override
    public String url() {
        return "https://api.stripe.com/v1/checkout/sessions/" + checkoutSessionId + "/line_items?limit=100";
    }
}
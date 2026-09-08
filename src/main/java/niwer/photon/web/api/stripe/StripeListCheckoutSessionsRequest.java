package niwer.photon.web.api.stripe;

import niwer.photon.objects.stripe.StripeCheckoutSessionList;

public class StripeListCheckoutSessionsRequest extends StripeApiRequest<StripeCheckoutSessionList> {

    private final String query;

    public StripeListCheckoutSessionsRequest(String startingAfter, int limit) {
        super(StripeCheckoutSessionList.class);
        this.query = "status=complete&limit=" + Math.max(1, Math.min(limit, 100))
            + (startingAfter == null || startingAfter.isBlank() ? "" : "&starting_after=" + this.encode(startingAfter));
    }

    @Override public String url() { return "https://api.stripe.com/v1/checkout/sessions?" + query; }
}
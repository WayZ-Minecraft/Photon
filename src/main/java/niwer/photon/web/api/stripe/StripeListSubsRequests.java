package niwer.photon.web.api.stripe;

import niwer.photon.objects.stripe.StripeSubscriptionList;

public class StripeListSubsRequests extends StripeApiRequest<StripeSubscriptionList> {

    private final String startingAfter;
    private final int limit;

    public StripeListSubsRequests(String startingAfter, int limit) {
        super(StripeSubscriptionList.class);
        this.startingAfter = startingAfter;
        this.limit = limit;
    }

    @Override
    public String url() {
        final StringBuilder path = new StringBuilder("https://api.stripe.com/v1/subscriptions?status=all&limit=").append(Math.max(1, Math.min(limit, 100)));
        if (startingAfter != null && !startingAfter.isBlank()) path.append("&starting_after=").append(this.encode(startingAfter));
        return path.toString();
    }
}

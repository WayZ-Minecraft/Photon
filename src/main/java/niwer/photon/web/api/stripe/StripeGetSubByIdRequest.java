package niwer.photon.web.api.stripe;

import niwer.photon.objects.stripe.StripeSubscription;

public class StripeGetSubByIdRequest extends StripeApiRequest<StripeSubscription> {

    private final String subscriptionId;

    public StripeGetSubByIdRequest(String subscriptionId) { this(subscriptionId, false); }

    public StripeGetSubByIdRequest(String payloadOrId, boolean isPayload) {
        super(StripeSubscription.class);
        this.subscriptionId = this.encode(isPayload ? extractDataObjectId(payloadOrId) : payloadOrId);
    }

    @Override
    public String url() {
        return "https://api.stripe.com/v1/subscriptions/" + this.subscriptionId;
    }
}

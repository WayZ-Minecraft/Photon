package niwer.photon.web.api.stripe;

public class StripeGetCustomerRequest extends StripeApiRequest {

    private final String customerId;

    public StripeGetCustomerRequest(String customerId) {
        super(null);
        this.customerId = this.encode(customerId);
    }

    @Override
    public String url() {
        return "https://api.stripe.com/v1/customers/" + this.customerId;
    }
}

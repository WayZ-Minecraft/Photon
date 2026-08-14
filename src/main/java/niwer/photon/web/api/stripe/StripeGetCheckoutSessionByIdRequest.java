package niwer.photon.web.api.stripe;

import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.objects.stripe.StripeCheckoutSession;

/**
 * This class is used to retrieve a specific checkout session from Stripe by its ID.
 * 
 * @author Niwer
 */
public class StripeGetCheckoutSessionByIdRequest extends StripeApiRequest<StripeCheckoutSession> {

    private final String checkoutSessionId;

    public StripeGetCheckoutSessionByIdRequest(String checkoutSessionId) { this(checkoutSessionId, false); }

    public StripeGetCheckoutSessionByIdRequest(String payloadOrId, boolean isPayload) {
        super(StripeCheckoutSession.class);
        this.checkoutSessionId = this.encode(isPayload ? extractDataObjectId(payloadOrId) : payloadOrId);
    }

    public static void main(String[] args) {
        Directories.load();
        var x = new StripeGetCheckoutSessionByIdRequest("cs_test_b1Rj3YZfFnONQZKY5fW5fGvbYQwnJsXH59m1JnTKwvTiTb3Dc6Y4H7AKkH").request();
        Console.debug(x);
    }

    @Override
    public String url() {
        return "https://api.stripe.com/v1/checkout/sessions/" + this.checkoutSessionId;
    }
}

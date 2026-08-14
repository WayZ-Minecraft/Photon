package niwer.photon.web.api.stripe;

/**
 * This class is used to retrieve a specific invoice from Stripe by its ID.
 * 
 * @author Niwer
 */
public class StripeGetInvoiceByIdRequest extends StripeApiRequest {

    private final String invoiceId;

    public StripeGetInvoiceByIdRequest(String invoiceId) { this(invoiceId, false); }

    public StripeGetInvoiceByIdRequest(String payloadOrId, boolean isPayload) {
        super(null);
        this.invoiceId = this.encode(isPayload ? extractDataObjectId(payloadOrId) : payloadOrId);
    }

    @Override
    public String url() {
        return "https://api.stripe.com/v1/invoices/" + this.invoiceId;
    }
}

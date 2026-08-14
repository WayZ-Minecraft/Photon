package niwer.photon.web.api.stripe;

import niwer.photon.objects.stripe.StripeCustomer;
import niwer.photon.objects.stripe.StripeInvoice;
import niwer.photon.objects.stripe.StripeSubscription;

public class StripeGetCustomerRequest extends StripeApiRequest<StripeCustomer> {

    private final String customerId;

    public StripeGetCustomerRequest(String customerId) {
        super(StripeCustomer.class);
        this.customerId = this.encode(customerId);
    }

    /**
     * Resolve the customer from a subscription, using the customer ID and latest invoice if necessary.
     * 
     * @param subscription The subscription to resolve the customer from
     * @return The resolved StripeCustomer object
     */
    public static StripeCustomer resolveCustomerFromSubscription(StripeSubscription subscription) {
        final StripeCustomer CUSTOMER = new StripeGetCustomerRequest(subscription.customerId()).request();
        if (CUSTOMER.email() != null && !CUSTOMER.email().isBlank()) return CUSTOMER;

        /* If the customer email is missing, try to resolve it from the latest invoice */
        final StripeInvoice LATEST_INVOICE = new StripeGetInvoiceByIdRequest(subscription.latestInvoice()).request();
        if (LATEST_INVOICE != null) return resolveCustomerFromInvoice(LATEST_INVOICE);

        return CUSTOMER;
    }

    /**
     * Resolve the customer from an invoice, using the customer email and ID.
     * 
     * @param invoice The invoice to resolve the customer from
     * @return The resolved StripeCustomer object
     */
    public static StripeCustomer resolveCustomerFromInvoice(StripeInvoice invoice) {
        if (invoice != null && invoice.customerEmail() != null && !invoice.customerEmail().isBlank()) return new StripeCustomer(invoice.customerEmail(), invoice.customerId(), invoice.id());
        return null;
    }

    @Override
    public String url() {
        return "https://api.stripe.com/v1/customers/" + this.customerId;
    }
}
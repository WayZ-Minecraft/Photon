package niwer.photon.objects.stripe;

import com.google.gson.annotations.SerializedName;

/**
 * @author Niwer 
 */
public class StripeInvoice {

    @SerializedName("id") private String id;
    @SerializedName("customer") private String customerId;
    @SerializedName("customer_email") private String customerEmail;

    public String id() { return id; }

    public String customerId() { return customerId; }

    public String customerEmail() { return customerEmail; }
}

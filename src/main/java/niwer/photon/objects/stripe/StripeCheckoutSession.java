package niwer.photon.objects.stripe;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Stripe Checkout Session object, which contains information about a specific checkout session.
 * This class is used to deserialize the JSON response from Stripe's API into a Java object.
 * 
 * @note Not all fields from the Stripe API response are included in this class. Only the most relevant fields are represented.
 * @author Niwer
 */
public class StripeCheckoutSession {

    @SerializedName("id") private String id;
    @SerializedName("client_reference_id") private String clientRefId;
    @SerializedName("custom_fields") private List<CustomField> customFields;
    @SerializedName("customer") private String customerID;
    @SerializedName("customer_details") private CustomerDetails customerDetails;
    @SerializedName("customer_email") private String customerEmail;
    @SerializedName("invoice") private String invoiceId;
    @SerializedName("subscription") private String subscriptionId;

    public String id() { return this.id; }

    public String clientRefId() { return this.clientRefId; }

    public List<CustomField> customFields() { return this.customFields; }

    public String customerID() { return this.customerID; }

    public CustomerDetails customerDetails() { return this.customerDetails; }

    public String customerEmail() { return this.customerEmail; }

    public String invoiceId() { return this.invoiceId; }

    public String subscriptionId() { return this.subscriptionId; }

    public static class CustomField {
        @SerializedName("key") private String key;
        @SerializedName("optional") private boolean optional;
        @SerializedName("text") private CustomFieldText text;

        public String key() { return this.key; }

        public boolean isOptional() { return this.optional; }

        public CustomFieldText text() { return this.text; }
    }

    public static class CustomFieldText {
        @SerializedName("default_value") private String defaultValue;
        @SerializedName("value") private String value;

        public String defaultValue() { return this.defaultValue; }

        public String value() { return this.value; }
    }

    public static class CustomerDetails {
        @SerializedName("email") private String email;

        public String email() { return this.email; }
    }
}

package niwer.photon.objects.stripe;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Stripe Customer object, which contains information about a specific customer in Stripe.
 * This class is used to deserialize the JSON response from Stripe's API into a Java object.
 * 
 * @note Not all fields from the Stripe API response are included in this class. Only the most relevant fields are represented.
 * @author Niwer
 */
public class StripeCustomer {

    @SerializedName("id") private String id;
    @SerializedName("email") private String email;
    @SerializedName("name") private String name;

    public StripeCustomer(String email, String name, String id) {
        this.email = email;
        this.name = name;
        this.id = id;
    }

    public String id() { return this.id; }

    public String email() { return this.email; }

    public String name() { return this.name; }
}

package niwer.photon.objects.stripe;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;

/**
 * @author Niwer 
 */
public class StripeSubscription {

    @SerializedName("id") private String id;
    @SerializedName("customer") private String customerId;
    @SerializedName("latest_invoice") private String latestInvoice;
    @SerializedName("current_period_end") private String currentPeriodEnd;
    @SerializedName("status") private String status;
    @SerializedName("metadata") private Map<String, String> metadata = Map.of();

    public String id() { return id; }

    public String customerId() { return customerId; }

    public String latestInvoice() { return latestInvoice; }

    public String productId() { return metadata == null ? null : metadata.get("product_id"); }

    public SubscriptionStatus status() {
        if (this.status == null || this.status.isBlank()) return SubscriptionStatus.EXPIRED;
        if (this.status.equalsIgnoreCase("active") || this.status.equalsIgnoreCase("trialing") || this.status.equalsIgnoreCase("past_due")) return SubscriptionStatus.ACTIVE;
        return SubscriptionStatus.EXPIRED;
    }
}

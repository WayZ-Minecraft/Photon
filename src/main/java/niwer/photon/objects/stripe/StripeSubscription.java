package niwer.photon.objects.stripe;

import com.google.gson.annotations.SerializedName;

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

    public String id() { return id; }

    public String customerId() { return customerId; }

    public String latestInvoice() { return latestInvoice; }

    public SubscriptionStatus status() {
        if (this.status == null || this.status.isBlank()) return SubscriptionStatus.EXPIRED;
        if (this.status.equalsIgnoreCase("active") || this.status.equalsIgnoreCase("trialing") || this.status.equalsIgnoreCase("past_due")) return SubscriptionStatus.ACTIVE;
        return SubscriptionStatus.EXPIRED;
    }
}

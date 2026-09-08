package niwer.photon.objects.stripe;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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
    @SerializedName("items") private Items items;

    public String id() { return id; }

    public String customerId() { return customerId; }

    public String latestInvoice() { return latestInvoice; }

    public String priceId() { return items == null ? null : items.priceId(); }

    public String productId() { return items == null ? null : items.productId(); }

    private static class Items {
        @SerializedName("data") private List<Item> data = List.of();

        public String priceId() {
            return data == null || data.isEmpty() || data.get(0) == null || data.get(0).price == null ? null : data.get(0).price.id;
        }

        public String productId() {
            return data == null || data.isEmpty() || data.get(0) == null || data.get(0).price == null ? null : data.get(0).price.product;
        }
    }

    private static class Item {
        @SerializedName("price") private Price price;
    }

    private static class Price {
        @SerializedName("id") private String id;
        @SerializedName("product") private String product;
    }

    public SubscriptionStatus status() {
        if (this.status == null || this.status.isBlank()) return SubscriptionStatus.EXPIRED;
        if (this.status.equalsIgnoreCase("active") || this.status.equalsIgnoreCase("trialing") || this.status.equalsIgnoreCase("past_due")) return SubscriptionStatus.ACTIVE;
        return SubscriptionStatus.EXPIRED;
    }
}

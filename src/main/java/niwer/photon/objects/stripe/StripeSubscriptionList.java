package niwer.photon.objects.stripe;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class StripeSubscriptionList {

    @SerializedName("data") public List<StripeSubscription> data;
    @SerializedName("has_more") public boolean hasMore;

    public List<StripeSubscription> data() { return data == null ? List.of() : data; }
    
    public boolean hasMore() { return hasMore; }

    /**
     * Returns the last subscription in the list, or null if the list is empty.
     * 
     * @return The last subscription in the list, or null if the list is empty.
     */
    public StripeSubscription last() {
        if (data == null || data.isEmpty()) return null;
        return data.get(data.size() - 1);
    }
}

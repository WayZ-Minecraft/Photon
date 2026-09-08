package niwer.photon.objects.stripe;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class StripeCheckoutSessionList {

    @SerializedName("data") private List<StripeCheckoutSession> data = List.of();
    @SerializedName("has_more") private boolean hasMore;

    public List<StripeCheckoutSession> data() { return data == null ? List.of() : data; }

    public boolean hasMore() { return hasMore; }

    public StripeCheckoutSession last() { return data().isEmpty() ? null : data().get(data().size() - 1); }
}
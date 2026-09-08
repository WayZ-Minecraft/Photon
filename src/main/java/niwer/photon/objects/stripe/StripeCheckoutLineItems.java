package niwer.photon.objects.stripe;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class StripeCheckoutLineItems {

    @SerializedName("data") private List<LineItem> data = List.of();

    public List<LineItem> data() { return data == null ? List.of() : data; }

    public static class LineItem {
        @SerializedName("price") private Price price;

        public String priceId() { return price == null ? null : price.id; }

        public String productId() { return price == null ? null : price.product; }
    }

    private static class Price {
        @SerializedName("id") private String id;
        @SerializedName("product") private String product;
    }
}
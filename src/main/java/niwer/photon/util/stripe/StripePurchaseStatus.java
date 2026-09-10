package niwer.photon.util.stripe;

public enum StripePurchaseStatus {
    ACTIVE,
    PENDING,
    CANCELED,
    EXPIRED,

    LINKING_PENDING,
    LINKED;

    public boolean isActive() {
        return this == ACTIVE || this == LINKED;
    }
}

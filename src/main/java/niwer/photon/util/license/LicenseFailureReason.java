package niwer.photon.util.license;

/**
 * Enumeration of possible reasons for license validation failure.
 * This is used to provide more specific feedback on why a license key is invalid, such as whether it's missing, expired, has an invalid hardware ID, etc.
 * 
 * @author Niwer
 */
public enum LicenseFailureReason {
    VALID,

    MISSING_LICENSE_KEY,
    PRODUCT_MISMATCH,
    HARDWARE_MISMATCH,

    EXPIRED,
    SUBSCRIPTION_INACTIVE,

    UNEXPECTED_ERROR
}

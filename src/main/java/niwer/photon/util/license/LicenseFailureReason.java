package niwer.photon.util.license;

/**
 * Enumeration of possible reasons for license validation failure.
 * This is used to provide more specific feedback on why a license key is invalid, such as whether it's missing, expired, has an invalid signature, etc.
 * 
 * @author Niwer
 */
public enum LicenseFailureReason {
    VALID,
    BYPASSED,
    MISSING_LICENSE_KEY,
    MISSING_PUBLIC_KEY,
    INVALID_TOKEN_FORMAT,
    INVALID_PAYLOAD,
    INVALID_SIGNATURE,
    PRODUCT_MISMATCH,
    EXPIRED,
    HARDWARE_MISMATCH,
    UNEXPECTED_ERROR
}

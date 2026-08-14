package niwer.photon.util.license;

import niwer.photon.objects.IPayloadProvider;

/**
 * Represents the result of a license validation attempt, including whether the license is valid, the reason for failure if invalid, and the claims contained in the license if valid.
 * 
 * @author Niwer
 */
public record LicenseValidationResult(boolean valid, LicenseFailureReason reason, String message, LicenseClaims claims) implements IPayloadProvider {
    
    public static LicenseValidationResult valid(final LicenseClaims claims) {
        return new LicenseValidationResult(true, LicenseFailureReason.VALID, "License is valid", claims);
    }

    public static LicenseValidationResult missing() {
        return new LicenseValidationResult(false, LicenseFailureReason.MISSING_LICENSE_KEY, "License key was not found in the Photon license database", null);
    }

    public static LicenseValidationResult invalid(final LicenseFailureReason reason, final String message) {
        return new LicenseValidationResult(false, reason, message, null);
    }
}

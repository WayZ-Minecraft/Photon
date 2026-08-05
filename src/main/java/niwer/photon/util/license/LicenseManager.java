package niwer.photon.util.license;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.LicenseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.os.OperatingSystem;

/**
 * Utility class for validating license keys for the Photon Network Engine.
 * 
 * @author Niwer
 */
public final class LicenseManager {

	private static final char[] LICENSE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

	private LicenseManager() {}

    /**
     * Generate a new random license key using the format "XXXXX-XXXXX-XXXXX-XXXXX" where X is an uppercase letter or digit, excluding confusing characters.
     * 
     * @return the generated license key
     */
	public static String generateLicenseKey() {
		final StringBuilder builder = new StringBuilder("");
		for (int group = 0; group < 4; group++) {
			if (group > 0) builder.append('-');
			for (int index = 0; index < 5; index++) builder.append(LICENSE_ALPHABET[ThreadLocalRandom.current().nextInt(LICENSE_ALPHABET.length)]);
		}
		return builder.toString();
	}

    /**
     * Issue a new license with the given information and store it in the database. The license key will be automatically generated and guaranteed to be unique.
     * 
     * @param productId The product id this license is valid for
     * @param customerName The name of the customer this license is issued to (optional)
     * @param customerEmail The email of the customer this license is issued to (optional)
     * @param orderId The associated order id for this license, if it was purchased through the official store (optional, but must be unique if provided)
     * @param expiresAtMillis The expiration date of the license in milliseconds since epoch, or null/0 for no expiration
     * @return the issued ObjectLicense containing all the license information, including the generated license key
     */
	public static ObjectLicense issueLicense(final String productId, final String name, final String customerEmail, final String creatorUuid, final Long expiresAtMillis) {
		final Date expiresAt = expiresAtMillis == null || expiresAtMillis <= 0L ? null : new Date(expiresAtMillis);
		String licenseKey = generateLicenseKey();
		while (LicenseTable.exists(licenseKey)) licenseKey = generateLicenseKey();
		return LicenseTable.issueLicense(licenseKey, productId, name, customerEmail, creatorUuid, expiresAt);
	}

	/**
     * Check if the given license key is valid for the expected product id, using the provided public key for signature verification.
     * 
     * @param licenseKey
     * @param publicKeyValue
     * @param expectedProductId
     * @return a LicenseValidationResult containing the validation result and claims if valid, or failure reason if invalid
     */
	public static LicenseValidationResult validateLicense(final String licenseKey, final String expectedProductId, final String hardwareId) {
		final String normalizedKey = LicenseTable.normalizeKey(licenseKey);
		final ObjectLicense license = LicenseTable.getByKey(normalizedKey);
		if (license == null) return LicenseValidationResult.invalid(LicenseFailureReason.MISSING_LICENSE_KEY, "License key was not found in the Photon license database");

		if (license.productId() == null || !license.productId().equalsIgnoreCase(expectedProductId)) return LicenseValidationResult.invalid(LicenseFailureReason.PRODUCT_MISMATCH, "License is not valid for product '" + expectedProductId + "'");
		if (LicenseTable.LicenseStatus.REVOKED == license.status()) return LicenseValidationResult.invalid(LicenseFailureReason.UNEXPECTED_ERROR, "License key has been revoked");
		if (license.isExpired()) return LicenseValidationResult.invalid(LicenseFailureReason.EXPIRED, "License key has expired");

		// Ensure creator's subscription is active; license becomes usable again if subscription restarts
		final boolean subscriptionActive = license.creatorUuid() != null && !license.creatorUuid().isBlank()
			? SubscriptionTable.isActive(license.customerEmail(), license.creatorUuid())
			: SubscriptionTable.isActive(license.customerEmail());
		if (!subscriptionActive) return LicenseValidationResult.invalid(LicenseFailureReason.SUBSCRIPTION_INACTIVE, "License creator subscription is not active");

		final String currentHardwareId = (hardwareId != null && !hardwareId.isBlank()) ? hardwareId : OperatingSystem.getHWID();
		if (license.hwid() != null && !license.hwid().isBlank()) {
			if (!license.hwid().equalsIgnoreCase(currentHardwareId)) return LicenseValidationResult.invalid(LicenseFailureReason.HARDWARE_MISMATCH, "License key is bound to another machine");
		} else if (currentHardwareId != null && !currentHardwareId.isBlank()) LicenseTable.activate(normalizedKey, currentHardwareId);

		return LicenseValidationResult.valid(new LicenseClaims(
			normalizedKey,
			license.productId(),
			license.name(),
			license.customerEmail(),
			currentHardwareId,
			license.createdAt() == null ? null : license.createdAt().getTime(),
			license.expiresAt() == null ? null : license.expiresAt().getTime()
		));
	}
}
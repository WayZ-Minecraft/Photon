package niwer.photon.util.license;

import java.util.Date;

import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.LicenseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.HashUtils;
import niwer.photon.util.os.OperatingSystem;

/**
 * Utility class for validating license keys for the Photon Network Engine.
 * 
 * @author Niwer
 */
public final class LicenseManager {

	private static final int LICENSE_KEY_GROUPS = 4;
	private static final int LICENSE_KEY_GROUP_LENGTH = 5;
	private static final char[] LICENSE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ1234567890".toCharArray();

	private LicenseManager() {}

	private static String generateLicenseKey(String customerName, String customerEmail, String creatorUuid, String productId) {
		final StringBuilder BUILDER = new StringBuilder("");
		final int HASH = HashUtils.hash(customerName + customerEmail + creatorUuid + productId).hashCode();

		/* Generate license key groups */
		for (int group = 0; group < LICENSE_KEY_GROUPS; group++) {
			if (group > 0) BUILDER.append('-'); // Add dash between groups
			for (int index = 0; index < LICENSE_KEY_GROUP_LENGTH; index++) BUILDER.append(LICENSE_ALPHABET[(Math.abs(HASH) + group * LICENSE_KEY_GROUP_LENGTH + index) % LICENSE_ALPHABET.length]);
		}

		return BUILDER.toString();
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
	public static ObjectLicense issueLicense(final String productId, final String customerName, final String customerEmail, final String creatorUuid, final Long expiresAtMillis) {
		final Date EXPIRES_AT = expiresAtMillis == null || expiresAtMillis <= 0L ? null : new Date(expiresAtMillis);
		final String LICENSE_KEY = generateLicenseKey(customerName, customerEmail, creatorUuid, productId);
		return LicenseTable.issueLicense(LICENSE_KEY, productId, customerName, customerEmail, creatorUuid, EXPIRES_AT);
	}

	/**
	 * Normalize a license key by trimming whitespace and converting to uppercase. This ensures consistent formatting for storage and comparison.
	 * 
	 * @param licenseKey The license key to normalize
	 * @return The normalized license key, or null if the input is null
	 */
	public static String normalizeKey(String licenseKey) { return licenseKey == null ? null : licenseKey.trim().toUpperCase(); }

	/**
     * Check if the given license key is valid for the expected product id, using the provided public key for signature verification.
     * 
     * @param licenseKey
     * @param publicKeyValue
     * @param expectedProductId
     * @return a LicenseValidationResult containing the validation result and claims if valid, or failure reason if invalid
     */
	public static LicenseValidationResult validateLicense(final String licenseKey, final String expectedProductId, final String hardwareId) {
		final String NORMALIZED_KEY = normalizeKey(licenseKey);
		final ObjectLicense license = LicenseTable.getByKey(NORMALIZED_KEY);
		if (license == null) return LicenseValidationResult.missing(); // License key not found in database

		if (license.productId() == null || !license.productId().equalsIgnoreCase(expectedProductId)) return LicenseValidationResult.invalid(LicenseFailureReason.PRODUCT_MISMATCH, "License is not valid for product '" + expectedProductId + "'");
		if (LicenseTable.LicenseStatus.REVOKED == license.status()) return LicenseValidationResult.invalid(LicenseFailureReason.UNEXPECTED_ERROR, "License key has been revoked");
		if (license.isExpired()) return LicenseValidationResult.invalid(LicenseFailureReason.EXPIRED, "License key has expired");

		/* Ensure creator's subscription is active; license becomes usable again if subscription restarts */
		final boolean IS_SUB_ACTIVE = license.creatorUuid() != null && !license.creatorUuid().isBlank() ? SubscriptionTable.isActive(license.customerEmail(), license.creatorUuid()) : SubscriptionTable.isActive(license.customerEmail());
		if (!IS_SUB_ACTIVE) return LicenseValidationResult.invalid(LicenseFailureReason.SUBSCRIPTION_INACTIVE, "License creator subscription is not active");

		final String CURREND_HWID = (hardwareId != null && !hardwareId.isBlank()) ? hardwareId : OperatingSystem.getHWID();
		if (license.hwid() != null && !license.hwid().isBlank()) { // If the key is already bound to a hardware id, ensure it matches the current machine's hwid
			if (!license.hwid().equalsIgnoreCase(CURREND_HWID)) return LicenseValidationResult.invalid(LicenseFailureReason.HARDWARE_MISMATCH, "License key is bound to another machine");
		} else if (CURREND_HWID != null && !CURREND_HWID.isBlank()) LicenseTable.activate(NORMALIZED_KEY, CURREND_HWID); // If the key is not bound to a hardware id, bind it to the current machine's hwid

		return LicenseValidationResult.valid(new LicenseClaims(
			NORMALIZED_KEY,
			license.productId(),
			license.name(),
			license.customerEmail(),
			CURREND_HWID,
			license.createdAt() == null ? null : license.createdAt().getTime(),
			license.expiresAt() == null ? null : license.expiresAt().getTime()
		));
	}
}
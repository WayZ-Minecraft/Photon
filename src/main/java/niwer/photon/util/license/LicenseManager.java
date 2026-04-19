package niwer.photon.util.license;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.JsonSyntaxException;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.LicenseTable;
import niwer.photon.util.os.OperatingSystem;

/**
 * Utility class for validating license keys for the Photon Network Engine.
 * 
 * @author Niwer
 */
public final class LicenseManager {

	private static final String LICENSE_SIGNATURE_ALGORITHM = "SHA256withRSA";
	private static final char[] LICENSE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

	private LicenseManager() {}
    
    /**
     * Check if the given license key is valid for the expected product id, using the provided public key for signature verification.
     * 
     * @param licenseKey
     * @param publicKeyValue
     * @param expectedProductId
     * @return a LicenseValidationResult containing the validation result and claims if valid, or failure reason if invalid
     */
	public static LicenseValidationResult validate(final String licenseKey, final String publicKeyValue, final String expectedProductId) {
		if (licenseKey == null || licenseKey.isBlank()) return LicenseValidationResult.invalid(LicenseFailureReason.MISSING_LICENSE_KEY, "Missing license key in network/config.json");
		if (!licenseKey.contains(".")) return validateDatabaseLicense(licenseKey, expectedProductId);
		if (publicKeyValue == null || publicKeyValue.isBlank()) return LicenseValidationResult.invalid(LicenseFailureReason.MISSING_PUBLIC_KEY, "Missing license public key in network/config.json");

		final String[] PARTS = licenseKey.trim().split("\\.");
		if (PARTS.length != 2) return LicenseValidationResult.invalid(LicenseFailureReason.INVALID_TOKEN_FORMAT, "License key must use the format payload.signature");
		try {
			final byte[] PAYLOAD_BYTES = Base64.getUrlDecoder().decode(PARTS[0]);
			final byte[] SIGNATURE_BYTES = Base64.getUrlDecoder().decode(PARTS[1]);
			final LicenseClaims CLAIMS = Directories.GSON.fromJson(new String(PAYLOAD_BYTES, StandardCharsets.UTF_8), LicenseClaims.class);

            /* Ensure the license claims are valid */
			if (CLAIMS == null) return LicenseValidationResult.invalid(LicenseFailureReason.INVALID_PAYLOAD, "License payload could not be parsed");
			if (CLAIMS.product_id() == null || !CLAIMS.product_id().equals(expectedProductId)) return LicenseValidationResult.invalid(LicenseFailureReason.PRODUCT_MISMATCH, "License is not valid for product '" + expectedProductId + "'");
			if (CLAIMS.expires_at() != null && CLAIMS.expires_at() > 0L && Instant.ofEpochMilli(CLAIMS.expires_at()).isBefore(Instant.now())) return LicenseValidationResult.invalid(LicenseFailureReason.EXPIRED, "License key has expired");
			if (CLAIMS.hardware_id() != null && !CLAIMS.hardware_id().isBlank()) {
				final String CURRENT_HARDWAIRE_ID = OperatingSystem.getHWID();
				if (!CLAIMS.hardware_id().equalsIgnoreCase(CURRENT_HARDWAIRE_ID)) return LicenseValidationResult.invalid(LicenseFailureReason.HARDWARE_MISMATCH, "License key is bound to another machine");
			}

            /* Verify the signature */
			final Signature VERIFIER = Signature.getInstance(LICENSE_SIGNATURE_ALGORITHM);
			VERIFIER.initVerify(loadPublicKey(publicKeyValue));
			VERIFIER.update(PAYLOAD_BYTES);
			if (!VERIFIER.verify(SIGNATURE_BYTES)) return LicenseValidationResult.invalid(LicenseFailureReason.INVALID_SIGNATURE, "License signature is invalid");

			return LicenseValidationResult.valid(CLAIMS);
		} catch (JsonSyntaxException e) {
			return LicenseValidationResult.invalid(LicenseFailureReason.INVALID_PAYLOAD, "License payload JSON is invalid");
		} catch (Exception e) {
			return LicenseValidationResult.invalid(LicenseFailureReason.UNEXPECTED_ERROR, "License validation failed: " + e.getMessage());
		}
	}

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
     * @param tebexOrderId The associated Tebex order id for this license, if it was purchased through the official Tebex store (optional, but must be unique if provided)
     * @param expiresAtMillis The expiration date of the license in milliseconds since epoch, or null/0 for no expiration
     * @return the issued ObjectLicense containing all the license information, including the generated license key
     */
	public static ObjectLicense issueLicense(final String productId, final String customerName, final String customerEmail, final String tebexOrderId, final Long expiresAtMillis) {
		final Date expiresAt = expiresAtMillis == null || expiresAtMillis <= 0L ? null : new Date(expiresAtMillis);
		String licenseKey = generateLicenseKey();
		while (LicenseTable.exists(licenseKey)) licenseKey = generateLicenseKey();
		return LicenseTable.issueLicense(licenseKey, productId, customerName, customerEmail, tebexOrderId, expiresAt);
	}

	private static LicenseValidationResult validateDatabaseLicense(final String licenseKey, final String expectedProductId) {
		final String normalizedKey = LicenseTable.normalizeKey(licenseKey);
		final ObjectLicense license = LicenseTable.getByKey(normalizedKey);
		if (license == null) return LicenseValidationResult.invalid(LicenseFailureReason.MISSING_LICENSE_KEY, "License key was not found in the Photon license database");

		if (license.productId == null || !license.productId.equalsIgnoreCase(expectedProductId)) return LicenseValidationResult.invalid(LicenseFailureReason.PRODUCT_MISMATCH, "License is not valid for product '" + expectedProductId + "'");
		if (LicenseTable.LicenseStatus.fromString(license.status) == LicenseTable.LicenseStatus.REVOKED) return LicenseValidationResult.invalid(LicenseFailureReason.UNEXPECTED_ERROR, "License key has been revoked");
		if (license.isExpired()) return LicenseValidationResult.invalid(LicenseFailureReason.EXPIRED, "License key has expired");

		final String currentHardwareId = OperatingSystem.getHWID();
		if (license.hwid != null && !license.hwid.isBlank()) {
			if (!license.hwid.equalsIgnoreCase(currentHardwareId)) return LicenseValidationResult.invalid(LicenseFailureReason.HARDWARE_MISMATCH, "License key is bound to another machine");
		} else if (currentHardwareId != null && !currentHardwareId.isBlank()) LicenseTable.activate(normalizedKey, currentHardwareId);

		return LicenseValidationResult.valid(new LicenseClaims(
			normalizedKey,
			license.productId,
			license.customerName,
			license.customerEmail,
			currentHardwareId,
			license.createdAt == null ? null : license.createdAt.getTime(),
			license.expiresAt == null ? null : license.expiresAt.getTime(),
			license.tebexOrderId
		));
	}

	private static PublicKey loadPublicKey(final String publicKeyValue) throws Exception {
		final String normalized = publicKeyValue
			.replace("-----BEGIN PUBLIC KEY-----", "")
			.replace("-----END PUBLIC KEY-----", "")
			.replaceAll("\\s", "");

		final byte[] encoded = Base64.getDecoder().decode(normalized);
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
    }
}
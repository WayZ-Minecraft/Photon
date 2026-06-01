package niwer.photon.util.license;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import niwer.photon.Directories;
import niwer.photon.util.os.OperatingSystem;

class LicenseManagerTest {

    @Test
    void generateLicenseKeyUsesTheExpectedAlphabetAndGrouping() {
        final String licenseKey = LicenseManager.generateLicenseKey();

        assertTrue(licenseKey.matches("[A-HJ-NP-Z2-9]{5}(?:-[A-HJ-NP-Z2-9]{5}){3}"));
    }

    @Test
    void validationFactoriesPreserveReasonAndClaims() {
        final LicenseClaims claims = new LicenseClaims("license-1", "product-1", "Alice", "alice@example.com", "hardware-1", 10L, 20L);

        final LicenseValidationResult valid = LicenseValidationResult.valid(claims);
        final LicenseValidationResult bypassed = LicenseValidationResult.bypassed("bypass");
        final LicenseValidationResult missing = LicenseValidationResult.missing("missing");
        final LicenseValidationResult invalid = LicenseValidationResult.invalid(LicenseFailureReason.EXPIRED, "expired");

        assertTrue(valid.valid());
        assertEquals(LicenseFailureReason.VALID, valid.reason());
        assertEquals(claims, valid.claims());

        assertTrue(bypassed.valid());
        assertEquals(LicenseFailureReason.BYPASSED, bypassed.reason());

        assertFalse(missing.valid());
        assertEquals(LicenseFailureReason.MISSING_LICENSE_KEY, missing.reason());

        assertFalse(invalid.valid());
        assertEquals(LicenseFailureReason.EXPIRED, invalid.reason());
    }

    @Test
    void validateReturnsExpectedFailuresForMalformedKeys() {
        assertEquals(LicenseFailureReason.MISSING_LICENSE_KEY, LicenseManager.validate(null, null, "product-1").reason());
        assertEquals(LicenseFailureReason.MISSING_LICENSE_KEY, LicenseManager.validate("", null, "product-1").reason());
        assertEquals(LicenseFailureReason.MISSING_PUBLIC_KEY, LicenseManager.validate("payload.signature", "", "product-1").reason());
        assertEquals(LicenseFailureReason.INVALID_TOKEN_FORMAT, LicenseManager.validate("payload.signature.extra", "public-key", "product-1").reason());
    }

    @Test
    void claimsRecordExposesItsValues() {
        final LicenseClaims claims = new LicenseClaims("license-1", "product-1", "Alice", "alice@example.com", "hardware-1", 10L, 20L);

        assertEquals("license-1", claims.license_id());
        assertEquals("product-1", claims.product_id());
        assertEquals("Alice", claims.customer_name());
        assertEquals("alice@example.com", claims.customer_email());
        assertEquals("hardware-1", claims.hardware_id());
        assertEquals(10L, claims.issued_at());
        assertEquals(20L, claims.expires_at());
    }

    @Test
    void failureReasonEnumIncludesExpectedValues() {
        assertTrue(LicenseFailureReason.values().length > 0);
        assertEquals(LicenseFailureReason.VALID, LicenseFailureReason.valueOf("VALID"));
    }

    @Test
    void validateAcceptsAProperlySignedPayload() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        final KeyPair keyPair = generator.generateKeyPair();

        final LicenseClaims claims = new LicenseClaims(
            "license-1",
            "product-1",
            "Alice",
            "alice@example.com",
            OperatingSystem.getHWID(),
            System.currentTimeMillis(),
            System.currentTimeMillis() + 86_400_000L
        );

        final String payload = Directories.GSON.toJson(claims);
        final Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(keyPair.getPrivate());
        signer.update(payload.getBytes(StandardCharsets.UTF_8));
        final String licenseKey = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());

        final String publicKey = "-----BEGIN PUBLIC KEY-----\n" + Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(keyPair.getPublic().getEncoded()) + "\n-----END PUBLIC KEY-----";

        final LicenseValidationResult result = LicenseManager.validate(licenseKey, publicKey, "product-1");

        assertTrue(result.valid());
        assertEquals(LicenseFailureReason.VALID, result.reason());
        assertNotNull(result.claims());
        assertEquals("product-1", result.claims().product_id());
    }

    @Test
    void validateRejectsInvalidPayloadJson() {
        final String payload = Base64.getUrlEncoder().withoutPadding().encodeToString("not-json".getBytes(StandardCharsets.UTF_8));

        assertEquals(LicenseFailureReason.INVALID_PAYLOAD, LicenseManager.validate(payload + ".AA", "public-key", "product-1").reason());
    }

    @Test
    void validateRejectsProductMismatchBeforeSignatureVerification() {
        final LicenseClaims claims = new LicenseClaims("license-1", "other-product", null, null, null, null, null);
        final String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(Directories.GSON.toJson(claims).getBytes(StandardCharsets.UTF_8));

        assertEquals(LicenseFailureReason.PRODUCT_MISMATCH, LicenseManager.validate(payload + ".AA", "public-key", "product-1").reason());
    }

    @Test
    void validateRejectsExpiredLicensesBeforeSignatureVerification() {
        final LicenseClaims claims = new LicenseClaims("license-1", "product-1", null, null, null, null, System.currentTimeMillis() - 1_000L);
        final String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(Directories.GSON.toJson(claims).getBytes(StandardCharsets.UTF_8));

        assertEquals(LicenseFailureReason.EXPIRED, LicenseManager.validate(payload + ".AA", "public-key", "product-1").reason());
    }

    @Test
    void validateRejectsHardwareMismatchBeforeSignatureVerification() {
        final LicenseClaims claims = new LicenseClaims("license-1", "product-1", null, null, "different-hwid", null, null);
        final String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(Directories.GSON.toJson(claims).getBytes(StandardCharsets.UTF_8));

        assertEquals(LicenseFailureReason.HARDWARE_MISMATCH, LicenseManager.validate(payload + ".AA", "public-key", "product-1").reason());
    }
}

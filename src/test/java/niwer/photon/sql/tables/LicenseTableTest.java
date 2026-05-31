package niwer.photon.sql.tables;

import java.util.Date;

import niwer.photon.objects.ObjectLicense;

public final class LicenseTableTest {

    private static boolean existsResult;
    private static String lastIssueLicenseKey;
    private static String lastIssueProductId;
    private static String lastIssueName;
    private static String lastIssueCustomerEmail;
    private static Date lastIssueExpiresAt;

    private LicenseTableTest() {}

    public static void reset() {
        existsResult = false;
        lastIssueLicenseKey = null;
        lastIssueProductId = null;
        lastIssueName = null;
        lastIssueCustomerEmail = null;
        lastIssueExpiresAt = null;
    }

    public static void setExistsResult(boolean result) {
        existsResult = result;
    }

    public static String normalizeKey(String licenseKey) {
        return licenseKey == null ? null : licenseKey.trim().toUpperCase();
    }

    public static boolean exists(String licenseKey) {
        return existsResult;
    }

    public static ObjectLicense issueLicense(String licenseKey, String productId, String name, String customerEmail, String creatorUuid, Date expiresAt) {
        lastIssueLicenseKey = licenseKey;
        lastIssueProductId = productId;
        lastIssueName = name;
        lastIssueCustomerEmail = customerEmail;
        lastIssueExpiresAt = expiresAt;
        return new ObjectLicense(normalizeKey(licenseKey), productId, name, customerEmail, creatorUuid, "ISSUED", expiresAt);
    }

    public static String lastIssueLicenseKey() { return lastIssueLicenseKey; }
    public static String lastIssueProductId() { return lastIssueProductId; }
    public static String lastIssueName() { return lastIssueName; }
    public static String lastIssueCustomerEmail() { return lastIssueCustomerEmail; }
    public static Date lastIssueExpiresAt() { return lastIssueExpiresAt; }
}

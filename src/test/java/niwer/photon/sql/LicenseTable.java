package niwer.photon.sql;

import java.util.Date;

import niwer.photon.objects.ObjectLicense;

public final class LicenseTable {

    private static boolean existsResult;
    private static String lastIssueLicenseKey;
    private static String lastIssueProductId;
    private static String lastIssueCustomerName;
    private static String lastIssueCustomerEmail;
    private static String lastIssueTebexOrderId;
    private static Date lastIssueExpiresAt;

    private LicenseTable() {}

    public static void reset() {
        existsResult = false;
        lastIssueLicenseKey = null;
        lastIssueProductId = null;
        lastIssueCustomerName = null;
        lastIssueCustomerEmail = null;
        lastIssueTebexOrderId = null;
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

    public static ObjectLicense issueLicense(String licenseKey, String productId, String customerName, String customerEmail, String tebexOrderId, Date expiresAt) {
        lastIssueLicenseKey = licenseKey;
        lastIssueProductId = productId;
        lastIssueCustomerName = customerName;
        lastIssueCustomerEmail = customerEmail;
        lastIssueTebexOrderId = tebexOrderId;
        lastIssueExpiresAt = expiresAt;
        return new ObjectLicense(normalizeKey(licenseKey), productId, customerName, customerEmail, tebexOrderId, "ISSUED", expiresAt);
    }

    public static String lastIssueLicenseKey() { return lastIssueLicenseKey; }
    public static String lastIssueProductId() { return lastIssueProductId; }
    public static String lastIssueCustomerName() { return lastIssueCustomerName; }
    public static String lastIssueCustomerEmail() { return lastIssueCustomerEmail; }
    public static String lastIssueTebexOrderId() { return lastIssueTebexOrderId; }
    public static Date lastIssueExpiresAt() { return lastIssueExpiresAt; }
}
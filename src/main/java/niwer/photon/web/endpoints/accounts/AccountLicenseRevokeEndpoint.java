package niwer.photon.web.endpoints.accounts;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.LicenseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseRevokeEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses/revoke"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final var account = UserSessionManager.requireAccount(handler);
        if (account == null) return;
        if (!SubscriptionTable.isActive(account.getEmail())) {
            handler.status(403).result("Active subscription required");
            return;
        }

        final JsonObject body = readBody(handler);
        final String licenseKey = getString(body, "license_key", "licenseKey", null);
        if (licenseKey == null || licenseKey.isBlank()) {
            handler.status(400).result("Missing license key");
            return;
        }

        final ObjectLicense license = LicenseTable.getByKey(licenseKey);
        if (license == null) {
            handler.status(404).result("License not found");
            return;
        }

        if (license.customerEmail() == null || !license.customerEmail().equalsIgnoreCase(account.getEmail())) {
            handler.status(403).result("You can only revoke your own licenses");
            return;
        }

        if (!LicenseTable.revoke(licenseKey)) {
            handler.status(500).result("Failed to revoke license");
            return;
        }

        handler.json(license);
    }

    private static JsonObject readBody(Context handler) {
        try {
            return Directories.GSON.fromJson(handler.body(), JsonObject.class);
        } catch (Exception ignored) {
            return new JsonObject();
        }
    }

    private static String getString(JsonObject body, String primaryKey, String secondaryKey, String defaultValue) {
        if (body != null && body.has(primaryKey) && !body.get(primaryKey).isJsonNull()) return body.get(primaryKey).getAsString();
        if (body != null && body.has(secondaryKey) && !body.get(secondaryKey).isJsonNull()) return body.get(secondaryKey).getAsString();
        return defaultValue;
    }
}
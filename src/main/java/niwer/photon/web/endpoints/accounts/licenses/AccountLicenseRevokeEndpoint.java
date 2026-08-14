package niwer.photon.web.endpoints.accounts.licenses;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.LicenseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.session.UserSessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.EndpointUtils;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseRevokeEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses/revoke"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final var account = UserSessionManager.requireAccount(handler);
        if (account == null) return;
        if (!SubscriptionTable.isActive(account.getEmail(), account.getUuid())) {
            handler.status(403).result("Active subscription required");
            return;
        }

        final JsonObject body = EndpointUtils.readBody(handler);
        final String licenseKey = GsonUtils.getString(body, "license_key", "licenseKey", null);
        if (licenseKey == null || licenseKey.isBlank()) {
            handler.status(400).result("Missing license key");
            return;
        }

        final ObjectLicense license = LicenseTable.getByKey(licenseKey);
        if (license == null) {
            handler.status(404).result("License not found");
            return;
        }

        if (license.creatorUuid() == null || !license.creatorUuid().equalsIgnoreCase(account.getUuid())) {
            handler.status(403).result("You can only revoke your own licenses");
            return;
        }

        if (!LicenseTable.revoke(licenseKey)) {
            handler.status(500).result("Failed to revoke license");
            return;
        }

        final ObjectLicense updatedLicense = LicenseTable.getByKey(licenseKey);
        handler.json(updatedLicense == null ? license.payload() : updatedLicense.payload());
    }
}
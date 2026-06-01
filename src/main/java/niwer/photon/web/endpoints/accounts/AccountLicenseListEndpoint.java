package niwer.photon.web.endpoints.accounts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.LicenseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseListEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        final var account = UserSessionManager.requireAccount(handler);
        if (account == null) return;
        if (!SubscriptionTable.isActive(account.getEmail(), account.getUuid())) {
            handler.status(403).result("Active subscription required");
            return;
        }

        final List<ObjectLicense> licenses = LicenseTable.getByCreatorUuid(account.getUuid());
        handler.json(licenses.stream().map(AccountLicenseListEndpoint::toPayload).toList());
    }

    private static Map<String, Object> toPayload(ObjectLicense license) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        final Long createdAt = license.createdAt() == null ? null : license.createdAt().getTime();
        final Long activatedAt = license.activatedAt() == null ? null : license.activatedAt().getTime();
        final Long expiresAt = license.expiresAt() == null ? null : license.expiresAt().getTime();

        payload.put("licenseKey", license.licenseKey());
        payload.put("license_key", license.licenseKey());
        payload.put("productId", license.productId());
        payload.put("product_id", license.productId());
        payload.put("name", license.name());
        payload.put("customerEmail", license.customerEmail());
        payload.put("customer_email", license.customerEmail());
        payload.put("creatorUuid", license.creatorUuid());
        payload.put("creator_uuid", license.creatorUuid());
        payload.put("hwid", license.hwid());
        payload.put("status", license.status());
        payload.put("createdAt", createdAt);
        payload.put("created_at", createdAt);
        payload.put("activatedAt", activatedAt);
        payload.put("activated_at", activatedAt);
        payload.put("expiresAt", expiresAt);
        payload.put("expires_at", expiresAt);
        return payload;
    }
}
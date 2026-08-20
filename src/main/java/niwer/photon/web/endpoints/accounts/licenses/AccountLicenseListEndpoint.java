package niwer.photon.web.endpoints.accounts.licenses;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.LicenseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.session.UserSessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseListEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 10, TimeUnit.SECONDS);

        final var account = UserSessionManager.requireAccount(handler);
        if (account == null) return;
        if (!SubscriptionTable.isActive(account.getEmail(), account.getUuid())) {
            handler.status(403).result("Active subscription required");
            return;
        }

        final List<ObjectLicense> licenses = LicenseTable.getByCreatorUuid(account.getUuid());
        handler.json(licenses.stream().map(license -> Objects.requireNonNull(license).payload()).toList());
    }
}
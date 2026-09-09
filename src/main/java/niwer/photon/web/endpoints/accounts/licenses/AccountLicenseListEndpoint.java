package niwer.photon.web.endpoints.accounts.licenses;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.LicenseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.session.SessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class AccountLicenseListEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/licenses"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 10, TimeUnit.SECONDS);

        final var ACCOUNT = SessionManager.requireAccount(handler);
        if (ACCOUNT == null) return;
        
        if (!SubscriptionTable.hasAnyAccess(ACCOUNT.getUuid())) {
            handler.status(403).result("Purchase or active subscription required");
            return;
        }

        final List<ObjectLicense> licenses = LicenseTable.getByCreatorUuid(ACCOUNT.getUuid());
        handler.json(licenses.stream().map(license -> Objects.requireNonNull(license).payload()).toList());
    }
}
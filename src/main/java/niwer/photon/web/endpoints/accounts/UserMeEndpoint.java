package niwer.photon.web.endpoints.accounts;

import java.util.Map;

import io.javalin.http.Context;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.web.WebServerEngine;
import niwer.photon.web.endpoints.IEndpoint;

public class UserMeEndpoint implements IEndpoint {

    @Override
    public String path() {
        return "/accounts/me";
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(Context handler) {
        final var account = WebServerEngine.USER_SESSION_MANAGER.requireAccount(handler);
        if (account == null)
            return;

        final Map<String, Object> response = account.toPublicMap();
        response.putAll(SubscriptionTable.subscriptionDetails(account.getEmail(), account.getUuid()));
        handler.json(response);
    }
}
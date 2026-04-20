package niwer.photon.web.endpoints.admin;

import io.javalin.http.Context;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminMeEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/me"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        final var account = AdminSessionManager.requireProjectAuthor(handler);
        if (account == null) return;

        handler.json(account);
    }
}
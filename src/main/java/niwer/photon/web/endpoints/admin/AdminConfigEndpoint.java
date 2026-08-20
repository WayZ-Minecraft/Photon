package niwer.photon.web.endpoints.admin;

import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.util.session.AdminSessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminConfigEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/config"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 5, TimeUnit.MINUTES);

        if (AdminSessionManager.requireAdministrator(handler) == null) return;
        handler.json(Directories.getConfig());
    }
}
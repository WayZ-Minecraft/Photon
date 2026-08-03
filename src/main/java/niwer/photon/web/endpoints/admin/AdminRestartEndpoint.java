package niwer.photon.web.endpoints.admin;

import io.javalin.http.Context;
import niwer.photon.PhotonEngine;
import niwer.photon.util.os.ApplicationUtils;
import niwer.photon.util.session.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminRestartEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/restart"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        if (AdminSessionManager.requireAdministrator(handler) == null) return;
        if (!AdminSessionManager.validateCsrf(handler)) { handler.status(403).result("Invalid CSRF token"); return; }

        handler.status(200).result("Restarting...");
        ApplicationUtils.restart(PhotonEngine.class, "--restart");
    }
}
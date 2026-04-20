package niwer.photon.web.endpoints;

import niwer.photon.PhotonEngine;
import niwer.photon.util.os.ApplicationUtils;
import niwer.photon.web.AdminSessionManager;

import io.javalin.http.Context;

/**
 * Endpoint to restart the network. This is a placeholder implementation and should be properly implemented to actually trigger a restart of the network application.
 * 
 * @author Niwer
 */
public class RestartEndpoint implements IEndpoint {

    @Override public String path() { return "/restart"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        if (AdminSessionManager.requireProjectAuthor(handler) == null) return;

        ApplicationUtils.restart(PhotonEngine.class, "--restart");
        handler.status(200).result("Restarting...");
    }

}

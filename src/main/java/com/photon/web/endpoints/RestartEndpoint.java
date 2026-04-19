package com.photon.web.endpoints;

import com.photon.network.NetworkEngine;
import com.photon.util.os.ApplicationUtils;

import io.javalin.http.Context;

/**
 * Endpoint to restart the network. This is a placeholder implementation and should be properly implemented to actually trigger a restart of the network application.
 * 
 * @author Niwer
 */
public class RestartEndpoint implements IEndpoint {

    @Override
    public String path() {
        return "/restart";
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public void handle(Context handler) {
        ApplicationUtils.restart(NetworkEngine.class);
        handler.status(200).result("Restarting...");
    }

}

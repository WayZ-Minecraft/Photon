package com.photon.web.endpoints;

import com.photon.Directories;

import io.javalin.http.Context;

public class NetworkConfigEndpoint implements IEndpoint {

    @Override
    public String path() {
        return "/api/network-config";
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(Context handler) {
        handler.json(Directories.getConfig());
    }
}
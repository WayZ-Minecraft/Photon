package com.photon.web.endpoints.servers;

import com.photon.sql.ServerTable;
import com.photon.web.endpoints.IEndpoint;

import io.javalin.http.Context;

public class ServerListEndpoint implements IEndpoint {

    @Override public String path() { return "/api/server-list"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        handler.json(ServerTable.getVisibleServers());
    }
}
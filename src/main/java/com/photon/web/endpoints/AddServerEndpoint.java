package com.photon.web.endpoints;

import com.photon.Directories;
import com.photon.objects.ObjectServer;
import com.photon.sql.ServerTable;

import io.javalin.http.Context;

public class AddServerEndpoint implements IEndpoint {

    @Override
    public String path() {
        return "/api/add-server";
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public void handle(Context handler) {
        final ObjectServer server;
        try {
            server = Directories.GSON.fromJson(handler.body(), ObjectServer.class);
        } catch (Exception e) {
            handler.status(400).result("Invalid server payload");
            return;
        }

        if (server == null) {
            handler.status(400).result("Invalid server payload");
            return;
        }

        final String remoteIp = handler.ip();
        if (server.serverIP == null || server.serverIP.isBlank() || !server.serverIP.equals(remoteIp)) {
            handler.status(403).result("Server IP mismatch");
            return;
        }

        if (server.serverPort <= 0 || server.serverPort > 65535) {
            handler.status(400).result("Invalid server port");
            return;
        }

        if (server.serverName == null || server.serverName.isBlank()) {
            handler.status(400).result("Server name is required");
            return;
        }

        server.serverName = server.serverName.trim();
        if (server.serverName.length() > 64) {
            server.serverName = server.serverName.substring(0, 64);
        }

        if (server.serverMOTD != null && server.serverMOTD.length() > 2048) {
            server.serverMOTD = server.serverMOTD.substring(0, 2048);
        }

        ServerTable.saveOrUpdate(server);
        handler.status(200).json(server);
    }

}

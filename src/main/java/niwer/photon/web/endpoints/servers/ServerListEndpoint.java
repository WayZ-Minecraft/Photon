package niwer.photon.web.endpoints.servers;

import niwer.photon.sql.ServerTable;
import niwer.photon.web.endpoints.IEndpoint;

import io.javalin.http.Context;

public class ServerListEndpoint implements IEndpoint {

    @Override public String path() { return "/servers/server-list"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        handler.json(ServerTable.getVisibleServers());
    }
}
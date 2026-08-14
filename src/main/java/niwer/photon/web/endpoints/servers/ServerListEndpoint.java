package niwer.photon.web.endpoints.servers;

import io.javalin.http.Context;
import niwer.photon.sql.ServerTable;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class ServerListEndpoint implements IEndpoint {

    @Override public String path() { return "/servers/server-list"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        handler.json(ServerTable.getVisibleServers());
    }
}
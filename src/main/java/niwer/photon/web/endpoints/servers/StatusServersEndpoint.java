package niwer.photon.web.endpoints.servers;

import io.javalin.http.Context;
import niwer.photon.sql.ServerTable;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class StatusServersEndpoint implements IEndpoint {

    @Override public String path() { return "/api/status/servers"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        handler.json(ServerTable.getAllServers());
    }
}
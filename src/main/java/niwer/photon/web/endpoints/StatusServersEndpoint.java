package niwer.photon.web.endpoints;

import io.javalin.http.Context;
import niwer.photon.sql.ServerTable;

public class StatusServersEndpoint implements IEndpoint {

    @Override public String path() { return "/api/status/servers"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        handler.json(ServerTable.getAllServers());
    }
}
package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.objects.ObjectServer;
import niwer.photon.sql.ServerTable;

class ServerListEndpointTest {

    @AfterEach
    void resetState() {
        ServerTable.reset();
    }

    @Test
    void exposesTheExpectedPathAndMethod() {
        final var endpoint = new niwer.photon.web.endpoints.servers.ServerListEndpoint();

        assertEquals("/api/server-list", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.GET, endpoint.method());
    }

    @Test
    void returnsVisibleServersAsJson() {
        final ObjectServer server = new ObjectServer();
        server.serverName = "Photon";
        server.serverIP = "127.0.0.1";
        server.serverPort = 25565;
        server.last_seen_at = new Date();
        ServerTable.setVisibleServers(List.of(server));

        final ContextStub stub = new ContextStub();
        new niwer.photon.web.endpoints.servers.ServerListEndpoint().handle(stub.context());

        assertEquals(List.of(server), stub.jsonBody());
    }
}
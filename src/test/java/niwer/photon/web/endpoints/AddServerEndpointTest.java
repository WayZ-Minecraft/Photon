package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.Directories;
import niwer.photon.objects.ObjectServer;
import niwer.photon.sql.tables.ServerTableTest;

class AddServerEndpointTest {

    @AfterEach
    void resetState() {
        ServerTableTest.reset();
        Directories.config = Directories.getConfig();
    }

    @Test
    void exposesTheExpectedPathAndMethod() {
        final var endpoint = new niwer.photon.web.endpoints.servers.AddServerEndpoint();

        assertEquals("/api/add-server", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsInvalidJsonPayload() {
        final ContextStubTest stub = new ContextStubTest().body("not-json");

        new niwer.photon.web.endpoints.servers.AddServerEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Invalid server payload", stub.resultBody());
    }

    @Test
    void rejectsNullJsonPayload() {
        final ContextStubTest stub = new ContextStubTest().body("null");

        new niwer.photon.web.endpoints.servers.AddServerEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Invalid server payload", stub.resultBody());
    }

    @Test
    void rejectsMismatchedIp() {
        final ContextStubTest stub = new ContextStubTest()
            .ip("127.0.0.1")
            .body(Directories.GSON.toJson(serverPayload("192.0.2.1", 25565, "Server")));

        new niwer.photon.web.endpoints.servers.AddServerEndpoint().handle(stub.context());

        assertEquals(403, stub.statusCode());
        assertEquals("Server IP mismatch", stub.resultBody());
    }

    @Test
    void rejectsInvalidPortAndMissingName() {
        final ContextStubTest invalidPort = new ContextStubTest()
            .ip("127.0.0.1")
            .body(Directories.GSON.toJson(serverPayload("127.0.0.1", 0, "Server")));

        new niwer.photon.web.endpoints.servers.AddServerEndpoint().handle(invalidPort.context());
        assertEquals(400, invalidPort.statusCode());
        assertEquals("Invalid server port", invalidPort.resultBody());

        final ContextStubTest missingName = new ContextStubTest()
            .ip("127.0.0.1")
            .body(Directories.GSON.toJson(serverPayload("127.0.0.1", 25565, "   ")));

        new niwer.photon.web.endpoints.servers.AddServerEndpoint().handle(missingName.context());
        assertEquals(400, missingName.statusCode());
        assertEquals("Server name is required", missingName.resultBody());
    }

    @Test
    void trimsAndSavesValidPayload() {
        final ObjectServer payload = serverPayload("127.0.0.1", 25565, "  Photon Server  ");
        payload.serverMOTD = "x".repeat(2050);
        payload.queuePort = 25566;
        payload.site = "https://example.com";
        payload.discord = "https://discord.gg/example";

        final ContextStubTest stub = new ContextStubTest()
            .ip("127.0.0.1")
            .body(Directories.GSON.toJson(payload));

        new niwer.photon.web.endpoints.servers.AddServerEndpoint().handle(stub.context());

        assertEquals(200, stub.statusCode());
        final ObjectServer saved = (ObjectServer) stub.jsonBody();
        assertEquals("Photon Server", saved.serverName);
        assertEquals(2048, saved.serverMOTD.length());
        assertEquals(saved, ServerTableTest.lastSavedServer());
        assertEquals("Photon Server", ServerTableTest.lastSavedServer().serverName);
    }

    @Test
    void truncatesLongServerNameAndAllowsNullMotd() {
        final ObjectServer payload = serverPayload("127.0.0.1", 25565, "x".repeat(80));
        payload.serverMOTD = null;

        final ContextStubTest stub = new ContextStubTest()
            .ip("127.0.0.1")
            .body(Directories.GSON.toJson(payload));

        new niwer.photon.web.endpoints.servers.AddServerEndpoint().handle(stub.context());

        assertEquals(200, stub.statusCode());
        final ObjectServer saved = (ObjectServer) stub.jsonBody();
        assertEquals(64, saved.serverName.length());
        assertNull(saved.serverMOTD);
    }

    private static ObjectServer serverPayload(String ip, int port, String name) {
        final ObjectServer server = new ObjectServer();
        server.serverIP = ip;
        server.serverPort = port;
        server.serverName = name;
        server.serverMOTD = "MOTD";
        server.queuePort = 25566;
        server.last_seen_at = new Date();
        return server;
    }
}

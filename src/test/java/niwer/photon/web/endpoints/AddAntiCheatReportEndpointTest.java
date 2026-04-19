package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import niwer.photon.sql.AnticheatTable;

class AddAntiCheatReportEndpointTest {

    @Test
    void exposesTheExpectedPathAndMethod() {
        final AddAntiCheatReportEndpoint endpoint = new AddAntiCheatReportEndpoint();

        assertEquals("/add-anticheat-report", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsMissingParameters() {
        final ContextStub stub = new ContextStub();

        new AddAntiCheatReportEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing parameters", stub.resultBody());
    }

    @Test
    void rejectsBlankParameters() {
        final ContextStub stub = new ContextStub()
            .formParam("fileMessage", "message")
            .formParam("fileName", "")
            .formParam("userUUID", "uuid")
            .formParam("operatingSystem", "Windows");

        new AddAntiCheatReportEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Parameters cannot be blank", stub.resultBody());
    }

    @Test
    void savesValidAntiCheatPayload() {
        final ContextStub stub = new ContextStub()
            .formParam("fileMessage", "Traceback")
            .formParam("fileName", "anticheat.log")
            .formParam("userUUID", "uuid-789")
            .formParam("operatingSystem", "Windows");

        new AddAntiCheatReportEndpoint().handle(stub.context());

        assertEquals("uuid-789", AnticheatTable.lastUserUUID());
        assertEquals("anticheat.log", AnticheatTable.lastFileName());
        assertEquals("Traceback", AnticheatTable.lastFileMessage());
        assertEquals("Windows", AnticheatTable.lastOperatingSystem());
        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
    }
}
package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import niwer.photon.sql.tables.AnticheatTableTest;

class AddAntiCheatReportEndpointTest {

    @Test
    void exposesTheExpectedPathAndMethod() {
        final AddAntiCheatReportEndpoint endpoint = new AddAntiCheatReportEndpoint();

        assertEquals("/add-anticheat-report", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsMissingParameters() {
        final ContextStubTest stub = new ContextStubTest();

        new AddAntiCheatReportEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing parameters", stub.resultBody());
    }

    @Test
    void rejectsBlankParameters() {
        final ContextStubTest stub = new ContextStubTest()
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
        final ContextStubTest stub = new ContextStubTest()
            .formParam("fileMessage", "Traceback")
            .formParam("fileName", "anticheat.log")
            .formParam("userUUID", "uuid-789")
            .formParam("operatingSystem", "Windows");

        new AddAntiCheatReportEndpoint().handle(stub.context());

        assertEquals("uuid-789", AnticheatTableTest.lastUserUUID());
        assertEquals("anticheat.log", AnticheatTableTest.lastFileName());
        assertEquals("Traceback", AnticheatTableTest.lastFileMessage());
        assertEquals("Windows", AnticheatTableTest.lastOperatingSystem());
        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
    }
}

package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import niwer.photon.sql.CrashReportTable;

class AddCrashReportEndpointTest {

    @Test
    void exposesTheExpectedPathAndMethod() {
        final AddCrashReportEndpoint endpoint = new AddCrashReportEndpoint();

        assertEquals("/add-crash-report", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsMissingParameters() {
        final ContextStub stub = new ContextStub();

        new AddCrashReportEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing parameters", stub.resultBody());
    }

    @Test
    void rejectsBlankParameters() {
        final ContextStub stub = new ContextStub()
            .formParam("fileMessage", "")
            .formParam("fileName", "crash.log")
            .formParam("userUUID", "uuid");

        new AddCrashReportEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Parameters cannot be blank", stub.resultBody());
    }

    @Test
    void savesValidCrashReportPayload() {
        final ContextStub stub = new ContextStub()
            .formParam("fileMessage", "Traceback")
            .formParam("fileName", "crash.log")
            .formParam("userUUID", "uuid-456");

        new AddCrashReportEndpoint().handle(stub.context());

        assertEquals("uuid-456", CrashReportTable.lastUserUUID());
        assertEquals("crash.log", CrashReportTable.lastFileName());
        assertEquals("Traceback", CrashReportTable.lastFileMessage());
        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
    }
}
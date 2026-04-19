package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import niwer.photon.sql.tables.HWIDTableTest;

class AddHWIDEndpointTest {

    @Test
    void exposesTheExpectedPathAndMethod() {
        final AddHWIDEndpoint endpoint = new AddHWIDEndpoint();

        assertEquals("/add-hwid", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsMissingParameters() {
        final ContextStubTest stub = new ContextStubTest();

        new AddHWIDEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing parameters", stub.resultBody());
    }

    @Test
    void rejectsBlankParameters() {
        final ContextStubTest stub = new ContextStubTest()
            .formParam("hwid", " ")
            .formParam("userUUID", "uuid")
            .formParam("operatingSystem", "Windows");

        new AddHWIDEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Parameters cannot be blank", stub.resultBody());
    }

    @Test
    void savesValidHwIDPayload() {
        final ContextStubTest stub = new ContextStubTest()
            .formParam("hwid", "hwid-123")
            .formParam("userUUID", "uuid-123")
            .formParam("operatingSystem", "Windows");

        new AddHWIDEndpoint().handle(stub.context());

        assertEquals("uuid-123", HWIDTableTest.lastUserUUID());
        assertEquals("hwid-123", HWIDTableTest.lastHWID());
        assertEquals("Windows", HWIDTableTest.lastOperatingSystem());
        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
    }
}

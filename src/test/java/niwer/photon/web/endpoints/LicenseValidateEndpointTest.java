package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

class LicenseValidateEndpointTest {

    @Test
    void exposesTheExpectedPathAndMethod() {
        final LicenseValidateEndpoint endpoint = new LicenseValidateEndpoint();

        assertEquals("/api/licenses/validate", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsMissingLicenseKey() {
        final ContextStubTest stub = new ContextStubTest();

        new LicenseValidateEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing license key", stub.resultBody());
    }

    @Test
    void returnsStructuredValidationPayloadForInvalidKey() throws Exception {
        final ContextStubTest stub = new ContextStubTest()
            .formParam("license_key", "payload.signature")
            .formParam("product_id", "niwer-engine")
            .formParam("hardware_id", "hwid-1");

        new LicenseValidateEndpoint().handle(stub.context());

        assertEquals(200, stub.statusCode());
        assertNotNull(stub.jsonBody());
        final Map<?, ?> payload = (Map<?, ?>) stub.jsonBody();
        assertEquals(false, payload.get("valid"));
        assertEquals(false, payload.get("canLaunch"));
    }
}

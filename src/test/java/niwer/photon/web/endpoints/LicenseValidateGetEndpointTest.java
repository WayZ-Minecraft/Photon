package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

class LicenseValidateGetEndpointTest {

    @Test
    void exposesTheExpectedPathAndMethod() {
        final LicenseValidateGetEndpoint endpoint = new LicenseValidateGetEndpoint();

        assertEquals("/api/licenses/validate", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.GET, endpoint.method());
    }

    @Test
    void rejectsMissingLicenseKey() {
        final ContextStubTest stub = new ContextStubTest();

        new LicenseValidateGetEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing license key", stub.resultBody());
    }

    @Test
    void supportsManualQueryStringValidation() {
        final ContextStubTest stub = new ContextStubTest()
            .queryParam("license_key", "payload.signature")
            .queryParam("product_id", "niwer-engine")
            .queryParam("hardware_id", "hwid-manual-test");

        new LicenseValidateGetEndpoint().handle(stub.context());

        assertEquals(200, stub.statusCode());
        assertNotNull(stub.jsonBody());

        final Map<?, ?> payload = (Map<?, ?>) stub.jsonBody();
        assertEquals(false, payload.get("valid"));
        assertEquals(false, payload.get("canLaunch"));
    }
}

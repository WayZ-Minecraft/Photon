package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RestartEndpointTest {

    @Test
    void exposesTheExpectedPathAndMethod() {
        final RestartEndpoint endpoint = new RestartEndpoint();

        assertEquals("/restart", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }
}
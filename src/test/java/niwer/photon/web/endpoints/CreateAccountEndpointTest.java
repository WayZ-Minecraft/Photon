package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.sql.tables.PlayerAccountTableTest;

class CreateAccountEndpointTest {

    @AfterEach
    void resetState() {
        PlayerAccountTableTest.reset();
    }

    @Test
    void exposesTheExpectedPathAndMethod() {
        final var endpoint = new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint();

        assertEquals("/accounts/create_account", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsMissingParameters() {
        final ContextStubTest stub = new ContextStubTest();

        new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing parameters", stub.resultBody());
    }

    @Test
    void rejectsBlankParameters() {
        final ContextStubTest stub = new ContextStubTest()
            .formParam("username", " ")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret");

        new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Parameters cannot be blank", stub.resultBody());
    }

    @Test
    void rejectsExistingEmail() {
        PlayerAccountTableTest.setEmailExistsResult(true);

        final ContextStubTest stub = new ContextStubTest()
            .formParam("username", "alice")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret");

        new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint().handle(stub.context());

        assertEquals("alice@example.com", PlayerAccountTableTest.lastEmailChecked());
        assertEquals(400, stub.statusCode());
        assertEquals("An account with this email already exists.", stub.resultBody());
    }

    @Test
    void returnsWithoutSettingAResponseWhenEmailIsAvailable() {
        PlayerAccountTableTest.setEmailExistsResult(false);

        final ContextStubTest stub = new ContextStubTest()
            .formParam("username", "alice")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret");

        new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint().handle(stub.context());

        assertEquals("alice@example.com", PlayerAccountTableTest.lastEmailChecked());
        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
    }
}

package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.sql.PlayerAccountTable;

class CreateAccountEndpointTest {

    @AfterEach
    void resetState() {
        PlayerAccountTable.reset();
    }

    @Test
    void exposesTheExpectedPathAndMethod() {
        final var endpoint = new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint();

        assertEquals("/accounts/create_account", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsMissingParameters() {
        final ContextStub stub = new ContextStub();

        new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing parameters", stub.resultBody());
    }

    @Test
    void rejectsBlankParameters() {
        final ContextStub stub = new ContextStub()
            .formParam("username", " ")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret");

        new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Parameters cannot be blank", stub.resultBody());
    }

    @Test
    void rejectsExistingEmail() {
        PlayerAccountTable.setEmailExistsResult(true);

        final ContextStub stub = new ContextStub()
            .formParam("username", "alice")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret");

        new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint().handle(stub.context());

        assertEquals("alice@example.com", PlayerAccountTable.lastEmailChecked());
        assertEquals(400, stub.statusCode());
        assertEquals("An account with this email already exists.", stub.resultBody());
    }

    @Test
    void returnsWithoutSettingAResponseWhenEmailIsAvailable() {
        PlayerAccountTable.setEmailExistsResult(false);

        final ContextStub stub = new ContextStub()
            .formParam("username", "alice")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret");

        new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint().handle(stub.context());

        assertEquals("alice@example.com", PlayerAccountTable.lastEmailChecked());
        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
    }
}
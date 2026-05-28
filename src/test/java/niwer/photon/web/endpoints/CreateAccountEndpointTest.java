package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.objects.ObjectPlayerAccount;

class CreateAccountEndpointTest {

    @AfterEach
    void resetState() {
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
        final ContextStubTest stub = new ContextStubTest()
            .formParam("username", "alice")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret123");

        final var endpoint = new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint() {
            @Override
            protected boolean emailExists(String email) {
                assertEquals("alice@example.com", email);
                return true;
            }
        };

        endpoint.handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("An account with this email already exists. Sign in instead.", stub.resultBody());
    }

    @Test
    void returnsWithoutSettingAResponseWhenEmailIsAvailable() {
        final ContextStubTest stub = new ContextStubTest()
            .formParam("username", "alice")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret123");
        final ObjectPlayerAccount createdAccount = new ObjectPlayerAccount();

        final var endpoint = new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint() {
            @Override
            protected boolean emailExists(String email) {
                assertEquals("alice@example.com", email);
                return false;
            }

            @Override
            protected boolean usernameExists(String username) {
                assertEquals("alice", username);
                return false;
            }

            @Override
            protected ObjectPlayerAccount createAccount(String username, String email, String password) {
                assertEquals("alice", username);
                assertEquals("alice@example.com", email);
                assertEquals("secret123", password);
                return createdAccount;
            }
        };

        endpoint.handle(stub.context());

        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
        assertEquals(createdAccount, stub.jsonBody());
    }
}

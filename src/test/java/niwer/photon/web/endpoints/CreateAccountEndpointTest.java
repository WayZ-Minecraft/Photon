package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

            @Override
            protected boolean hasActiveSubscription(String email, String accountUuid) {
                return true;
            }

            @Override
            protected niwer.photon.web.UserSessionManager.AuthSession createSession(String email, String password) {
                return new niwer.photon.web.UserSessionManager.AuthSession("session-token", createdAccount);
            }
        };

        endpoint.handle(stub.context());

        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
        assertNotNull(stub.jsonBody());
        assertTrue(stub.jsonBody().toString().contains("session-token"));
    }

    @Test
    void acceptsCheckoutSessionIdInsteadOfPurchaseToken() {
        final ContextStubTest stub = new ContextStubTest()
            .formParam("username", "alice")
            .formParam("email", "alice@example.com")
            .formParam("password", "secret123")
            .formParam("checkoutSessionId", "cs_test_123");
        final ObjectPlayerAccount createdAccount = new ObjectPlayerAccount();

        final var endpoint = new niwer.photon.web.endpoints.accounts.CreateAccountEndpoint() {
            @Override
            protected boolean emailExists(String email) {
                return false;
            }

            @Override
            protected boolean usernameExists(String username) {
                return false;
            }

            @Override
            protected ObjectPlayerAccount createAccount(String username, String email, String password) {
                return createdAccount;
            }

            @Override
            protected boolean canRedeemPurchaseReference(String purchaseReference) {
                assertEquals("cs_test_123", purchaseReference);
                return true;
            }

            @Override
            protected boolean redeemPurchaseReference(String purchaseReference, ObjectPlayerAccount account) {
                assertEquals("cs_test_123", purchaseReference);
                assertEquals(createdAccount, account);
                return true;
            }

            @Override
            protected boolean hasActiveSubscription(String email, String accountUuid) {
                return true;
            }

            @Override
            protected niwer.photon.web.UserSessionManager.AuthSession createSession(String email, String password) {
                return new niwer.photon.web.UserSessionManager.AuthSession("session-token", createdAccount);
            }
        };

        endpoint.handle(stub.context());

        assertNull(stub.statusCode());
        assertNull(stub.resultBody());
        assertNotNull(stub.jsonBody());
        assertTrue(stub.jsonBody().toString().contains("session-token"));
    }
}

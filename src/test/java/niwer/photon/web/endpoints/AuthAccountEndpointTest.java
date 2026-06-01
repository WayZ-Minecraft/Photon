package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.util.PasswordHasher;

class AuthAccountEndpointTest {

    @Test
    void signsInWithEmailAndPasswordOnly() {
        final ObjectPlayerAccount account = account("alice", "alice@example.com", PasswordHasher.hash("secret"));
        final ContextStubTest stub = new ContextStubTest()
            .formParam("email", "alice@example.com")
            .formParam("password", "secret");

        final var endpoint = new niwer.photon.web.endpoints.accounts.AuthAccountEndpoint() {
            @Override
            protected ObjectPlayerAccount lookupAccountByEmail(String email) {
                assertEquals("alice@example.com", email);
                return account;
            }

            @Override
            protected niwer.photon.web.UserSessionManager.AuthSession createSession(String email, String password) {
                assertEquals("alice@example.com", email);
                assertEquals("secret", password);
                return new niwer.photon.web.UserSessionManager.AuthSession("session-token", account);
            }

            @Override
            protected java.util.Map<String, Object> accountResponse(ObjectPlayerAccount currentAccount) {
                return currentAccount.toPublicMap();
            }
        };

        endpoint.handle(stub.context());

        assertNull(stub.statusCode());
        assertNotNull(stub.jsonBody());
        final String response = stub.jsonBody().toString();
        assertTrue(response.contains("session-token"));
        assertTrue(response.contains("alice@example.com"));
    }

    @Test
    void rejectsMissingEmailOrPassword() {
        final ContextStubTest stub = new ContextStubTest().formParam("password", "secret");

        new niwer.photon.web.endpoints.accounts.AuthAccountEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing parameters", stub.resultBody());
    }

    private static ObjectPlayerAccount account(String username, String email, String password) {
        final ObjectPlayerAccount account = new ObjectPlayerAccount();
        setField(account, "username", username);
        setField(account, "email", email);
        setField(account, "password", password);
        setField(account, "uuid", "uuid-123");
        setField(account, "administrator", false);
        return account;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            final Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set test field " + fieldName, exception);
        }
    }
}
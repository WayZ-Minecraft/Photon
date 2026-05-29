package niwer.photon.web.endpoints.accounts;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * Endpoint to handle account authentication. This is a placeholder implementation and should be properly implemented with necessary validations, password hashing, and database storage.
 * 
 * @author Niwer
 */
public class AuthAccountEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/auth_account"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final String email = handler.formParam("email");
        final String password = handler.formParam("password");

        /* Ensure all parameters are provided */
        if(email == null || password == null) {
            handler.status(400).result("Missing parameters");
            return;
        }

        /* Ensure no parameters are blank */
        if(email.isBlank() || password.isBlank()) {
            handler.status(400).result("Parameters cannot be blank");
            return;
        }

        /* Authenticate the user */
        final ObjectPlayerAccount ACCOUNT = lookupAccountByEmail(email);
        if(ACCOUNT == null) {
            handler.status(401).result("No account found with the provided email");
            return;
        }
        if(!ACCOUNT.password().equals(password)) {
            handler.status(401).result("Incorrect password");
            return;
        }

        final UserSessionManager.AuthSession session = UserSessionManager.login(email, password);
        if (session == null) {
            handler.status(401).result("Invalid credentials or access denied");
            return;
        }

        final var account = ACCOUNT.toPublicMap();
        account.putAll(SubscriptionTable.subscriptionDetails(ACCOUNT.getEmail()));
        handler.json(new LoginResponse(session.token(), account));
    }

    protected ObjectPlayerAccount lookupAccountByEmail(String email) {
        return PlayerAccountTable.getAccountByEmail(email);
    }

    private record LoginResponse(String token, Object account) {}
}

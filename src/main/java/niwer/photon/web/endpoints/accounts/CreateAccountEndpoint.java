package niwer.photon.web.endpoints.accounts;

import java.util.regex.Pattern;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * Endpoint to handle account creation. This is a placeholder implementation and should be properly implemented with necessary validations, password hashing, and database storage.
 * 
 * @author Niwer
 */
public class CreateAccountEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/create_account"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final String username = handler.formParam("username");
        final String email = handler.formParam("email");
        final String password = handler.formParam("password");

        /* Ensure all parameters are provided */
        if(username == null || email == null || password == null) {
            handler.status(400).result("Missing parameters");
            return;
        }

        /* Ensure no parameters are blank */
        if(username.isBlank() || email.isBlank() || password.isBlank()) {
            handler.status(400).result("Parameters cannot be blank");
            return;
        }

        /* Ensure the email address is valid */
        if(!validEmailAddress(email)) {
            handler.status(400).result("Invalid email address");
            return;
        }

        /* Ensure the password is at least 8 characters long */
        if(password.length() < 8) {
            handler.status(400).result("Password must be at least 8 characters long");
            return;
        }

        /* Ensure the email address is not already in use */
        if(emailExists(email)) {
            handler.status(400).result("An account with this email already exists. Sign in instead.");
            return;
        }

        /* Ensure the username is not already in use */
        if(usernameExists(username)) {
            handler.status(400).result("An account with this username already exists.");
            return;
        }

        if (!SubscriptionTable.isActive(email)) {
            handler.status(403).result("An active subscription is required to create an account");
            return;
        }

        /* Create the account */
        final ObjectPlayerAccount ACCOUNT = createAccount(username, email, password);
        if(ACCOUNT == null) {
            handler.status(500).result("Failed to create account");
            return;
        }

        final UserSessionManager.AuthSession session = UserSessionManager.login(email, password);
        if (session == null) {
            handler.status(500).result("Failed to create session");
            return;
        }

        final var response = ACCOUNT.toPublicMap();
        response.putAll(SubscriptionTable.subscriptionDetails(ACCOUNT.getEmail()));
        handler.json(new LoginResponse(session.token(), response));
    }

    protected boolean emailExists(String email) {
        return PlayerAccountTable.emailExists(email);
    }

    protected boolean usernameExists(String username) {
        return PlayerAccountTable.usernameExists(username);
    }

    protected ObjectPlayerAccount createAccount(String username, String email, String password) {
        return PlayerAccountTable.createAccount(username, email, password);
    }

    private static boolean validEmailAddress(String email) {
		final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        return EMAIL_PATTERN.matcher(email).matches();
	}

        private record LoginResponse(String token, Object account) {}
}

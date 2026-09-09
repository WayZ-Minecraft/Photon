package niwer.photon.web.endpoints.accounts;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.session.AuthSession;
import niwer.photon.util.session.SessionManager;
import niwer.photon.util.session.SessionManager.Scope;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.EndpointUtils;
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
        IEndpoint.setupRateLimit(handler, 5, TimeUnit.MINUTES);
        
        final String username = handler.formParam("username");
        final String email = handler.formParam("email");
        final String password = handler.formParam("password");
        final String checkoutSessionId = EndpointUtils.firstNonBlank(handler.formParam("checkoutSessionId"), handler.formParam("token"));

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
        if(PlayerAccountTable.emailExists(email)) {
            handler.status(400).result("An account with this email already exists. Sign in instead.");
            return;
        }

        /* Ensure the username is not already in use */
        if(PlayerAccountTable.usernameExists(username)) {
            handler.status(400).result("An account with this username already exists.");
            return;
        }

        final boolean hasPurchaseReference = checkoutSessionId != null && !checkoutSessionId.isBlank();
        final ObjectSubscription subscription = SubscriptionTable.getByEmail(email);
        if (hasPurchaseReference) {
            if (!PurchaseTable.canRedeem(checkoutSessionId)) {
                handler.status(403).result("Invalid or expired purchase token");
                return;
            }
        }

        /* Create the account */
        final ObjectUserAccount ACCOUNT = PlayerAccountTable.createAccount(username, email, password);
        if(ACCOUNT == null) {
            handler.status(500).result("Failed to create account");
            return;
        }

        if (hasPurchaseReference && !PurchaseTable.redeem(checkoutSessionId, ACCOUNT)) {
            handler.status(500).result("Failed to link purchase token");
            return;
        } else if (subscription != null && subscription.isActive()) {
            SubscriptionTable.upsertSubscription(
                subscription.customerEmail(),
                subscription.customerName(),
                subscription.customerId(),
                subscription.subscriptionId(),
                subscription.status(),
                subscription.expiresAt(),
                ACCOUNT.getUuid()
            );
        }

        final AuthSession session = SessionManager.login(email, password, Scope.USER);
        if (session == null) {
            handler.status(500).result("Failed to create session");
            return;
        }

        handler.json(new LoginResponse(session.token(), ACCOUNT.payload()));
    }

    private static boolean validEmailAddress(String email) {
		final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        return EMAIL_PATTERN.matcher(email).matches();
	}

    private record LoginResponse(String token, Object account) {}
}

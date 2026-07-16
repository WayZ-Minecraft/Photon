package niwer.photon.web.endpoints.accounts;

import java.util.regex.Pattern;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.sql.PurchaseTokenTable;
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
        final String checkoutSessionId = firstNonBlank(handler.formParam("checkoutSessionId"), handler.formParam("token"));

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

        final boolean hasPurchaseReference = checkoutSessionId != null && !checkoutSessionId.isBlank();
        final ObjectSubscription subscription = SubscriptionTable.getByEmail(email);
        if (hasPurchaseReference) {
            if (!canRedeemPurchaseReference(checkoutSessionId)) {
                handler.status(403).result("Invalid or expired purchase token");
                return;
            }
        } else if (subscription == null || !subscription.isActive()) {
            handler.status(403).result("Active subscription required");
            return;
        }

        /* Create the account */
        final ObjectPlayerAccount ACCOUNT = createAccount(username, email, password);
        if(ACCOUNT == null) {
            handler.status(500).result("Failed to create account");
            return;
        }

        if (hasPurchaseReference && !redeemPurchaseReference(checkoutSessionId, ACCOUNT)) {
            PlayerAccountTable.deleteAccount(ACCOUNT.getUuid());
            handler.status(500).result("Failed to link purchase token");
            return;
        } else if (subscription != null && subscription.isActive()) {
            SubscriptionTable.upsertSubscription(
                subscription.customerEmail(),
                subscription.customerName(),
                subscription.customerId(),
                subscription.subscriptionId(),
                SubscriptionTable.SubscriptionStatus.fromString(subscription.status()),
                subscription.expiresAt(),
                ACCOUNT.getUuid()
            );
        }

        final UserSessionManager.AuthSession session = createSession(email, password);
        if (session == null) {
            handler.status(500).result("Failed to create session");
            return;
        }

        final var response = ACCOUNT.toPublicMap();
        response.putAll(SubscriptionTable.subscriptionDetails(ACCOUNT.getEmail(), ACCOUNT.getUuid()));
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

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }

    protected boolean canRedeemPurchaseReference(String purchaseReference) {
        return PurchaseTokenTable.canRedeem(purchaseReference);
    }

    protected boolean redeemPurchaseReference(String purchaseReference, ObjectPlayerAccount account) {
        return PurchaseTokenTable.redeem(purchaseReference, account);
    }

    protected boolean hasActiveSubscription(String email, String accountUuid) {
        return SubscriptionTable.isActive(email, accountUuid);
    }

    protected UserSessionManager.AuthSession createSession(String email, String password) {
        return UserSessionManager.login(email, password);
    }

        private record LoginResponse(String token, Object account) {}
}

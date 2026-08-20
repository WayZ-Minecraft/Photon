package niwer.photon.web.endpoints.accounts;

import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.session.AuthSession;
import niwer.photon.util.session.UserSessionManager;
import niwer.photon.web.HttpMethod;
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
        IEndpoint.setupRateLimit(handler, 5, TimeUnit.MINUTES);

        final String email = handler.formParam("email");
        final String password = handler.formParam("password");
        final String checkoutSessionId = firstNonBlank(handler.formParam("checkoutSessionId"), handler.formParam("token"));

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
        final ObjectUserAccount ACCOUNT = lookupAccountByEmail(email);
        if(ACCOUNT == null) {
            handler.status(401).result("No account found with the provided email");
            return;
        }
        if(!PlayerAccountTable.passwordMatches(ACCOUNT.password(), password)) {
            handler.status(401).result("Incorrect password");
            return;
        }

        if (!PlayerAccountTable.isArgon2Password(ACCOUNT.password())) PlayerAccountTable.setPassword(ACCOUNT.getUuid(), password);

        /* Redeem the token if provided and valid */
        final boolean hasPurchaseReference = checkoutSessionId != null && !checkoutSessionId.isBlank();
        final ObjectSubscription subscription = SubscriptionTable.getByEmail(email);
        if (hasPurchaseReference) {
            if (!PurchaseTable.canRedeem(checkoutSessionId)) {
                handler.status(403).result("Invalid or expired purchase token");
                return;
            }
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

        final AuthSession session = createSession(email, password);
        if (session == null) {
            handler.status(401).result("Invalid credentials or access denied");
            return;
        }

        handler.json(new LoginResponse(session.token(), accountResponse(ACCOUNT)));
    }

    protected ObjectUserAccount lookupAccountByEmail(String email) {
        return PlayerAccountTable.getAccountByEmail(email);
    }

    protected AuthSession createSession(String email, String password) {
        return UserSessionManager.login(email, password);
    }

    protected java.util.Map<String, Object> accountResponse(ObjectUserAccount account) {
        final var response = account.toPublicMap();
        response.putAll(SubscriptionTable.subscriptionDetails(account.getEmail(), account.getUuid()));
        return response;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }

    private record LoginResponse(String token, Object account) {}
}

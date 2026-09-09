package niwer.photon.web.endpoints.accounts;

import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.session.AuthSession;
import niwer.photon.util.session.SessionManager;
import niwer.photon.util.session.SessionManager.Scope;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.EndpointUtils;
import niwer.photon.web.endpoints.IEndpoint;

public class AuthAccountEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/auth_account"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 5, TimeUnit.MINUTES);

        final Credentials credentials = readCredentials(handler);
        if (credentials == null || credentials.email == null || credentials.password == null || credentials.email.isBlank() || credentials.password.isBlank()) {
            handler.status(400).result("Missing or blank parameters");
            return;
        }

        /* Try to login as admin first */
        final AuthSession adminSession = SessionManager.login(credentials.email, credentials.password, Scope.ADMIN);
        if (adminSession != null) {
            setupAdminCookies(handler, adminSession);
            handler.json(new LoginResponse(null, adminSession.account().payload(), true));
            return;
        }

        /* Standard user auth. */
        final ObjectUserAccount account = PlayerAccountTable.getAccountByEmail(credentials.email);
        if (account == null || !PlayerAccountTable.passwordMatches(account.password(), credentials.password)) {
            handler.status(401).result("Invalid credentials or access denied");
            return;
        }

        if (!PlayerAccountTable.isArgon2Password(account.password())) {
            PlayerAccountTable.setPassword(account.getUuid(), credentials.password);
        }

        // Handle Purchase and Subscription Linking
        final String checkoutSessionId = credentials.token;
        final boolean hasPurchaseReference = checkoutSessionId != null && !checkoutSessionId.isBlank();
        final ObjectSubscription subscription = SubscriptionTable.getByEmail(credentials.email);

        if (hasPurchaseReference) {
            if (!PurchaseTable.canRedeem(checkoutSessionId)) {
                handler.status(403).result("Invalid or expired purchase token");
                return;
            }
            if (!PurchaseTable.redeem(checkoutSessionId, account)) {
                handler.status(500).result("Failed to link purchase token");
                return;
            }
        } else if (subscription != null && subscription.isActive()) {
            SubscriptionTable.upsertSubscription(
                subscription.customerEmail(),
                subscription.customerName(),
                subscription.customerId(),
                subscription.subscriptionId(),
                subscription.status(),
                subscription.expiresAt(),
                account.getUuid()
            );
        }

        final AuthSession userSession = SessionManager.login(credentials.email, credentials.password, Scope.USER);
        if (userSession == null) {
            handler.status(401).result("Invalid credentials or access denied");
            return;
        }

        handler.json(new LoginResponse(userSession.token(), account.payload(), false));
    }

    private static void setupAdminCookies(Context handler, AuthSession session) {
        try {
            final String adminCookie = "photon_admin=" + session.token() + "; HttpOnly; Path=/; Max-Age=3600; SameSite=Strict";
            handler.res().addHeader("Set-Cookie", adminCookie);

            final String csrf = SessionManager.getCsrfForToken(session.token());
            if (csrf != null && !csrf.isBlank()) {
                final String csrfCookie = "photon_csrf=" + csrf + "; Path=/; Max-Age=3600; SameSite=Strict";
                handler.res().addHeader("Set-Cookie", csrfCookie);
            }
        } catch (Exception ignored) {}
    }

    private static Credentials readCredentials(Context handler) {
        String email = EndpointUtils.firstNonBlank(handler.formParam("email"), handler.queryParam("email"));
        String password = EndpointUtils.firstNonBlank(handler.formParam("password"), handler.queryParam("password"));
        String token = EndpointUtils.firstNonBlank(handler.formParam("checkoutSessionId"), handler.formParam("token"));

        if (email != null && password != null) return new Credentials(email, password, token);

        try {
            Credentials jsonCreds = GsonUtils.GSON.fromJson(handler.body(), Credentials.class);
            if (jsonCreds != null) return jsonCreds;
        } catch (Exception ignored) {}

        return null;
    }

    private record Credentials(String email, String password, String token) {}
    private record LoginResponse(String token, Object account, boolean isAdmin) {}
}
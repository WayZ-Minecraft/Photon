package niwer.photon.web.endpoints.accounts;

import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.HashUtils;
import niwer.photon.util.session.Session;
import niwer.photon.util.session.SessionManager;
import niwer.photon.util.session.SessionManager.Scope;
import niwer.photon.util.stripe.EntitlementManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.EndpointUtils;
import niwer.photon.web.endpoints.IEndpoint;

public class AuthAccountEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/auth_account"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 5, TimeUnit.MINUTES);

        final Credentials CREDENTIALS = readCredentials(handler);
        if (CREDENTIALS == null || CREDENTIALS.email == null || CREDENTIALS.password == null || CREDENTIALS.email.isBlank() || CREDENTIALS.password.isBlank()) {
            handler.status(400).result("Missing or blank parameters");
            return;
        }

        /* Try to login as admin first */
        final Session ADMIN_SESSION = SessionManager.login(CREDENTIALS.email, CREDENTIALS.password, Scope.ADMIN);
        if (ADMIN_SESSION != null) {
            setupAdminCookies(handler, ADMIN_SESSION);
            handler.json(new LoginResponse(null, ADMIN_SESSION.account().payload(), true));
            return;
        }

        /* Standard user auth. */
        final ObjectUserAccount ACCOUNT = PlayerAccountTable.getAccountByEmail(CREDENTIALS.email);
        if (ACCOUNT == null || !HashUtils.passwordMatches(ACCOUNT.password(), CREDENTIALS.password)) {
            handler.status(401).result("Invalid credentials or access denied");
            return;
        }

        final String CHECKOUT_SESSION_ID = CREDENTIALS.token;
        if (CHECKOUT_SESSION_ID != null && !CHECKOUT_SESSION_ID.isBlank()) {
            if (!EntitlementManager.redeemPurchase(CHECKOUT_SESSION_ID, ACCOUNT)) {
                handler.status(403).result("Invalid or expired purchase token");
                return;
            }
        }

        final Session USER_AUTH = SessionManager.login(CREDENTIALS.email, CREDENTIALS.password, Scope.USER);
        if (USER_AUTH == null) {
            handler.status(401).result("Invalid credentials or access denied");
            return;
        }

        handler.json(new LoginResponse(USER_AUTH.token(), ACCOUNT.payload(), false));
    }

    private static void setupAdminCookies(Context handler, Session session) {
        try {
            final String ADMIN_COOKIE = "photon_admin=" + session.token() + "; HttpOnly; Path=/; Max-Age=3600; SameSite=Strict";
            handler.res().addHeader("Set-Cookie", ADMIN_COOKIE);

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
        String token = EndpointUtils.firstNonBlank(
            handler.formParam("checkoutSessionId"),
            handler.formParam("token"),
            handler.queryParam("checkoutSessionId"),
            handler.queryParam("token")
        );

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
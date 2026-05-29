package niwer.photon.web.endpoints.tebex;

import java.util.UUID;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.sql.SubscriptionTable;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class TebexLoginEndpoint implements IEndpoint {

    @Override public String path() { return "/tebex/login"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        if (!isValidTebexSecret(handler.header("X-Photon-Secret"), handler.queryParam("secret"))) {
            handler.status(401).result("Unauthorized");
            return;
        }

        final String email = firstNonBlank(handler.queryParam("email"), handler.queryParam("customer_email"), handler.queryParam("user_email"));
        if (email == null || email.isBlank()) {
            handler.status(400).result("Missing email");
            return;
        }

        final String name = firstNonBlank(handler.queryParam("name"), handler.queryParam("customer_name"), "");
        final String tebexCustomerId = firstNonBlank(handler.queryParam("customer_id"), handler.queryParam("customerId"), "");
        final String tebexSubscriptionId = firstNonBlank(handler.queryParam("subscription_id"), handler.queryParam("subscriptionId"), "");
        final String expiresAtStr = firstNonBlank(handler.queryParam("expires_at"), handler.queryParam("expiresAt"), null);

        final Long expiresAt = parseLongSafe(expiresAtStr);
        final boolean expired = false;
        final SubscriptionStatus status = expired ? SubscriptionStatus.EXPIRED : SubscriptionStatus.ACTIVE;

        /* Upsert the subscription record so the account becomes a subscriber if applicable */
        SubscriptionTable.upsertSubscription(email, name, tebexCustomerId, tebexSubscriptionId, status, expiresAt == null ? null : new java.util.Date(expiresAt));

        /* Find or create an account for this email */
        ObjectPlayerAccount account = PlayerAccountTable.getAccountByEmail(email);
        if (account == null) {
            final String usernameBase = email.split("@")[0].replaceAll("[^A-Za-z0-9]", "");
            final String username = (usernameBase == null || usernameBase.isBlank() ? "user" : usernameBase) + "-" + UUID.randomUUID().toString().substring(0,4);
            final String password = UUID.randomUUID().toString();
            account = PlayerAccountTable.createAccount(username, email, password);
            if (account == null) {
                handler.status(500).result("Failed to create account");
                return;
            }
        }

        final var session = UserSessionManager.createSessionForAccount(account);
        if (session == null) {
            handler.status(500).result("Failed to create session");
            return;
        }

        final String returnTo = firstNonBlank(handler.queryParam("return"), handler.queryParam("return_to"), handler.queryParam("redirect"), handler.queryParam("redirect_url"));
        if (returnTo != null && !returnTo.isBlank()) {
            final String redirectUrl = returnTo.contains("?") ? (returnTo + "&token=" + session.token()) : (returnTo + "?token=" + session.token());
            handler.redirect(redirectUrl);
            return;
        }

        final var response = account.toPublicMap();
        response.putAll(SubscriptionTable.subscriptionDetails(account.getEmail()));
        handler.json(new LoginResponse(session.token(), response));
    }

    private static boolean isValidTebexSecret(String headerSecret, String querySecret) {
        final String expected = Directories.getConfig().tebex_webhook_secret;
        if (expected == null || expected.isBlank()) return true;
        return expected.equals(headerSecret) || expected.equals(querySecret);
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private static Long parseLongSafe(String s) {
        try { return s == null ? null : Long.parseLong(s); } catch (Exception e) { return null; }
    }

    private record LoginResponse(String token, Object account) {}
}

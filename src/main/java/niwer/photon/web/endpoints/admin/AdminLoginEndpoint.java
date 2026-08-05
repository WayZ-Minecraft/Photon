package niwer.photon.web.endpoints.admin;

import io.javalin.http.Context;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.session.AuthSession;
import niwer.photon.web.WebServerEngine;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminLoginEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/login"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final Credentials credentials = readCredentials(handler);
        if (credentials == null || credentials.email == null || credentials.password == null) {
            handler.status(400).result("Missing parameters");
            return;
        }
        
        final AuthSession session = WebServerEngine.ADMIN_SESSION_MANAGER.login(credentials.email, credentials.password);
        if (session == null) {
            handler.status(401).result("Invalid credentials or access denied");
            return;
        }

        // Set HttpOnly cookie with admin session token and a non-HttpOnly CSRF cookie
        try {
            final String adminCookie = "photon_admin=" + session.token() + "; HttpOnly; Path=/; Max-Age=3600; SameSite=Strict";
            handler.res().addHeader("Set-Cookie", adminCookie);
            // Retrieve csrf token from session map (session stored in AdminSessionManager)
            final String csrf = WebServerEngine.ADMIN_SESSION_MANAGER.getCsrfForToken(session.token());
            if (csrf != null && !csrf.isBlank()) {
                final String csrfCookie = "photon_csrf=" + csrf + "; Path=/; Max-Age=3600; SameSite=Strict";
                // csrf cookie is intentionally NOT HttpOnly so client JS can read it for the X-CSRF-Token header
                handler.res().addHeader("Set-Cookie", csrfCookie);
            }
        } catch (Exception ignored) {}

        // Return account details (no token) — client will use cookie-based auth for subsequent admin requests
        handler.json(new LoginResponse(session.account().toPublicMap()));
    }

    private static Credentials readCredentials(Context handler) {
        final String email = firstNonBlank(handler.formParam("email"), handler.queryParam("email"));
        final String password = firstNonBlank(handler.formParam("password"), handler.queryParam("password"));
        if (email != null && password != null) return new Credentials(email, password);

        try {
            final Credentials jsonCredentials = GsonUtils.GSON.fromJson(handler.body(), Credentials.class);
            if (jsonCredentials != null) return jsonCredentials;
        } catch (Exception ignored) {}

        return null;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }

    private record Credentials(String email, String password) {}
    private record LoginResponse(Object account) {}
}
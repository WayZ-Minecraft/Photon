package niwer.photon.web.endpoints.admin;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.web.AdminSessionManager;
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

        final AdminSessionManager.AuthSession session = AdminSessionManager.login(credentials.email, credentials.password);
        if (session == null) {
            handler.status(401).result("Invalid credentials or access denied");
            return;
        }

        handler.json(new LoginResponse(session.token(), session.account()));
    }

    private static Credentials readCredentials(Context handler) {
        final String email = firstNonBlank(handler.formParam("email"), handler.queryParam("email"));
        final String password = firstNonBlank(handler.formParam("password"), handler.queryParam("password"));
        if (email != null && password != null) return new Credentials(email, password);

        try {
            final Credentials jsonCredentials = Directories.GSON.fromJson(handler.body(), Credentials.class);
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
    private record LoginResponse(String token, Object account) {}
}
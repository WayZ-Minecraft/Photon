package niwer.photon.web.endpoints;

import io.javalin.http.Context;

/**
 * GET variant for quick manual tests (browser/query-string based).
 */
public class LicenseValidateGetEndpoint implements IEndpoint {

    @Override public String path() { return "/api/licenses/validate"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        new LicenseValidateEndpoint().handle(handler);
    }
}

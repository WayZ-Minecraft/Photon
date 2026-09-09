package niwer.photon.web.endpoints.updates;

import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * Serves mod update files from GitHub releases.
 * TODO : In the future, this should be replaced by modrinth for all projects that are hosted there.
 * 
 * @author Niwer
 */
public class GetAssetsEndpoint implements IEndpoint {

    @Override public String path() { return "/download/list"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context ctx) {
        IEndpoint.setupRateLimit(ctx, 10, TimeUnit.MINUTES);

        ctx.status(200).result("This endpoint is not yet implemented.");
    }
}

package niwer.photon.web.endpoints.updates;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.api.github.DownloadAssetService;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * Serves mod update files from GitHub releases.
 * TODO : In the future, this should be replaced by modrinth for all projects that are hosted there.
 * 
 * @author Niwer
 */
public class DownloadEndpoint implements IEndpoint {

    @Override public String path() { return "/download/mod"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context ctx) {
        IEndpoint.setupRateLimit(ctx, 10, TimeUnit.MINUTES);

        /* Check if any asset id was provided as query parameter */
        final var ASSET_ID = ctx.queryParam("current_version");
        if (ASSET_ID == null) {
            ctx.status(400).result("Please provide the asset ID.");
            return;
        }

        /* Check if any repo was provided as query parameter */
        final var REPO = ctx.queryParam("repo");
        if (REPO == null) {
            ctx.status(400).result("Please provide the repository name.");
            return;
        }

        /* Check if any repo owner was provided as query parameter */
        final var OWNER = ctx.queryParam("owner");
        if (OWNER == null) {
            ctx.status(400).result("Please provide the repository owner.");
            return;
        }

        /* Get the latest version from GitHub */
        final String NAME = REPO+"-"+ASSET_ID+".jar"; //TODO ?
        final InputStream MOD_FILE = new DownloadAssetService(OWNER, REPO, Long.parseLong(ASSET_ID)).request();

        ctx.contentType("application/java-archive"); // Set the MIME type 
        ctx.header("Content-Disposition", "attachment; filename=\"" + NAME + "\""); // Tell the browser to treat it as a downloadable attachment with a filename
        ctx.status(200);
        ctx.result(MOD_FILE); // pass the input stream directly
    }
}

package niwer.photon.web.endpoints.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.javalin.http.Context;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.util.updater.UpdateChannel;
import niwer.photon.util.updater.UpdateFileType;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * Serves mod update files from the services_update directory.
 */
public class ModDownloadEndpoint implements IEndpoint {

    @Override public String path() { return "/download/mod"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context ctx) {
        /* Check if a channel was provided as query parameter */
        final var QUERY_CHANNEL = ctx.queryParam("channel");

        if (QUERY_CHANNEL == null || !UpdateChannel.isVariant(QUERY_CHANNEL)) {
            Console.log("Invalid or missing download channel: " + QUERY_CHANNEL).error().container(PhotonEngine.LOGGER).send();
            ctx.status(400); // BAD_REQUEST
            return;
        }

        /* Determine the file path based on update type and selected channel */
        final String FILE_PATH = Directories.getPathForUpdateChannel(UpdateFileType.MOD, UpdateChannel.valueOf(QUERY_CHANNEL.toUpperCase()));
        final File MOD_FILE = new File(FILE_PATH);

        /* Set the MIME type */
        ctx.contentType("application/java-archive");

        /* Tell the browser to treat it as a downloadable attachment with a filename */
        ctx.header("Content-Disposition", "attachment; filename=\"" + MOD_FILE.getName() + "\"");

        try {
            ctx.result(new FileInputStream(MOD_FILE)); // pass the input stream directly
        } catch (IOException e) {
            Console.log("Error serving mod file: " + e.getMessage()).error().send();
            ctx.status(500).result("Error serving file: " + e.getLocalizedMessage());
        }
    }

}

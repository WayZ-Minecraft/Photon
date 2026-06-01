package niwer.photon.web.endpoints.admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.util.os.OperatingSystem;
import niwer.photon.util.updater.UpdateChannel;
import niwer.photon.util.updater.UpdateFileType;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminUpdateEndpoint implements IEndpoint {

    @Override public String path() { return "/api/update"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        final String typeParam = handler.queryParam("type");
        final String channelParam = handler.queryParam("channel");
        final boolean metadataOnly = Boolean.parseBoolean(handler.queryParam("metadata")); // If true, only return metadata (SHA-1 and size) without the file content

        if (typeParam == null || channelParam == null) {
            handler.status(400).result("Missing parameters");
            return;
        }

        /* Get channel and type */
        final UpdateFileType TYPE;
        final UpdateChannel CHANNEL;
        try {
            TYPE = UpdateFileType.fromString(typeParam);
            CHANNEL = UpdateChannel.fromString(channelParam);
        } catch (IllegalArgumentException ex) {
            handler.status(400).result("Invalid update type or channel");
            return;
        }

        /* Get update file path */
        final String UPDATE_PATH = Directories.getPathForUpdateChannel(TYPE, CHANNEL);
        final File UPDATE_FILE = new File(UPDATE_PATH);
        if (!UPDATE_FILE.exists()) {
            handler.status(404).result("Update not found");
            return;
        }

        /* Read update file */
        try {
            final byte[] data = Files.readAllBytes(UPDATE_FILE.toPath());
            final String sha1 = OperatingSystem.hash(UPDATE_FILE, "SHA-1");

            if (metadataOnly) {
                handler.json(new UpdateMetadata(sha1, data.length));
                return;
            }

            handler.header("Update-Sha1", sha1);
            handler.contentType("application/octet-stream");
            handler.result(data);
        } catch (IOException e) {
            handler.status(500).result("Unable to read update file");
        }
    }

    private record UpdateMetadata(String sha1, int size) {}
}
package niwer.photon.web.endpoints.admin;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.util.os.ApplicationUtils;
import niwer.photon.util.updater.UpdateChannel;
import niwer.photon.util.updater.UpdateFileType;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminUploadUpdateEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/updates/upload"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        if (AdminSessionManager.requireAdministrator(handler) == null) return;

        final UploadedFile uploadedFile = handler.uploadedFile("file");
        if (uploadedFile == null) {
            handler.status(400).result("Missing file");
            return;
        }

        final String fileName = uploadedFile.filename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".jar")) {
            handler.status(400).result("Invalid file type. Only JAR files are allowed");
            return;
        }

        final String fileTypeParam = handler.formParam("file_type");
        if (fileTypeParam == null || fileTypeParam.isBlank()) {
            handler.status(400).result("Missing file_type");
            return;
        }

        final String channelParam = handler.formParam("channel");

        final UpdateFileType fileType;
        final UpdateChannel channel;
        try {
            fileType = UpdateFileType.fromString(fileTypeParam);
            channel = channelParam == null || channelParam.isBlank()
                ? UpdateChannel.STABLE
                : UpdateChannel.fromString(channelParam);
        } catch (IllegalArgumentException ex) {
            handler.status(400).result("Invalid file_type or channel");
            return;
        }

        final Path outputPath = Path.of(Directories.getPathForUpdateChannel(fileType, channel));

        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }

            try (InputStream input = uploadedFile.content()) {
                Files.copy(input, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }

            final boolean restartRequired = fileType == UpdateFileType.NETWORK;
            handler.json(new UploadResponse(
                true,
                restartRequired
                    ? "Update uploaded successfully. Network restart requested."
                    : "Update uploaded successfully.",
                fileType.name(),
                channel.name(),
                outputPath.toString(),
                restartRequired
            ));

            if (restartRequired) {
                ApplicationUtils.restart(PhotonEngine.class, "--restart");
            }
        } catch (Exception e) {
            handler.status(500).result("Failed to upload update file");
        }
    }

    private record UploadResponse(
        boolean success,
        String message,
        String fileType,
        String channel,
        String outputPath,
        boolean restartRequired
    ) {}
}

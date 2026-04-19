package niwer.photon.web.endpoints;

import niwer.photon.PhotonEngine;
import niwer.photon.sql.CrashReportTable;
import niwer.photon.util.PhotonLogTypes;

import io.javalin.http.Context;
import niwer.lumen.Console;

public class AddCrashReportEndpoint implements IEndpoint {

    @Override public String path() { return "/add-crash-report"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final String fileMessage = handler.formParam("fileMessage");
        final String fileName = handler.formParam("fileName");
        final String userUUID = handler.formParam("userUUID");

        /* Ensure all parameters are provided */
        if (fileMessage == null || fileName == null || userUUID == null) {
            handler.status(400).result("Missing parameters");
            return;
        }

        /* Ensure parameters are not blank */
        if (fileMessage.isBlank() || fileName.isBlank() || userUUID.isBlank()) {
            handler.status(400).result("Parameters cannot be blank");
            return;
        }

        CrashReportTable.save(userUUID, fileName, fileMessage);
        Console.log("Crash report received: " + userUUID).sendToProcessor().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
    }
}
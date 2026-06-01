package niwer.photon.web.endpoints;

import niwer.photon.PhotonEngine;
import niwer.photon.sql.CrashReportTable;
import niwer.photon.sql.CrashReportTable.CrashReportSides;
import niwer.photon.util.PhotonLogTypes;

import io.javalin.http.Context;
import niwer.lumen.Console;

public class AddCrashReportEndpoint implements IEndpoint {

    @Override public String path() { return "/add-crash-report"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final String FILE_MESSAGE = handler.formParam("fileMessage");
        final String FILE_NAME = handler.formParam("fileName");
        final String USER_UUID = handler.formParam("userUUID");
        final String SIDE = handler.formParam("side");

        /* Ensure all parameters are provided */
        if (FILE_MESSAGE == null || FILE_NAME == null || USER_UUID == null) {
            handler.status(400).result("Missing parameters");
            return;
        }

        /* Ensure parameters are not blank */
        if (FILE_MESSAGE.isBlank() || FILE_NAME.isBlank() || USER_UUID.isBlank() || (SIDE != null && SIDE.isBlank())) {
            handler.status(400).result("Parameters cannot be blank");
            return;
        }

        final CrashReportSides reportSide = SIDE == null ? CrashReportSides.CLIENT : CrashReportSides.fromString(SIDE);
        CrashReportTable.save(USER_UUID, FILE_NAME, FILE_MESSAGE, reportSide);
        Console.log("Crash report received: " + USER_UUID).sendToProcessor().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
    }
}
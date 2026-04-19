package niwer.photon.web.endpoints;

import niwer.photon.PhotonEngine;
import niwer.photon.sql.AnticheatTable;
import niwer.photon.util.PhotonLogTypes;

import io.javalin.http.Context;
import niwer.lumen.Console;

public class AddAntiCheatReportEndpoint implements IEndpoint {

    @Override public String path() { return "/add-anticheat-report"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final String FILE_MESSAGE = handler.formParam("fileMessage");
        final String FILE_NAME = handler.formParam("fileName");
        final String USER_UUID = handler.formParam("userUUID");
        final String OPERATRING_SYSTEM = handler.formParam("operatingSystem");

        /* Ensure all parameters are provided */
        if (FILE_MESSAGE == null || FILE_NAME == null || USER_UUID == null || OPERATRING_SYSTEM == null) {
            handler.status(400).result("Missing parameters");
            return;
        }

        /* Ensure parameters are not blank */
        if (FILE_MESSAGE.isBlank() || FILE_NAME.isBlank() || USER_UUID.isBlank() || OPERATRING_SYSTEM.isBlank()) {
            handler.status(400).result("Parameters cannot be blank");
            return;
        }

        AnticheatTable.save(USER_UUID, FILE_NAME, FILE_MESSAGE, OPERATRING_SYSTEM);
        Console.log("Anticheat report received: " + USER_UUID).sendToProcessor().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
    }
}
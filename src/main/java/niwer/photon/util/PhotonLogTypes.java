package niwer.photon.util;

import org.slf4j.LoggerFactory;

import niwer.lumen.Console;
import niwer.lumen.EnumLogColor;
import niwer.lumen.types.BasicLogType;
import niwer.lumen.types.ILogType;
import niwer.photon.PhotonEngine;

public class PhotonLogTypes {
    public static final ILogType NETWORK = new BasicLogType("NETWORK", EnumLogColor.GREEN);
    public static final ILogType SQL = new BasicLogType("SQL", EnumLogColor.RED);
    public static final ILogType DISCORD_BOT = new BasicLogType("DISCORD_BOT", EnumLogColor.CYAN);
    public static final ILogType WEB_SERVER = new BasicLogType("WEB_SERVER", EnumLogColor.YELLOW);
    public static final ILogType STRIPE = new BasicLogType("STRIPE", EnumLogColor.BLUE);

    private PhotonLogTypes() {}

    public static final void silenceLogsFor(String loggerName) {
        final var logger = LoggerFactory.getLogger(loggerName);

        if (logger instanceof ch.qos.logback.classic.Logger logbackLogger) logbackLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        else Console.log("Logback not bound to SLF4J; unable to set '%s' log level dynamically.", loggerName).error().container(PhotonEngine.LOGGER).send();
    } 
}

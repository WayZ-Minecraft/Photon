package niwer.photon.util;

import niwer.lumen.EnumLogColor;
import niwer.lumen.types.BasicLogType;
import niwer.lumen.types.ILogType;

public class PhotonLogTypes {
    public static final ILogType NETWORK = new BasicLogType("NETWORK", EnumLogColor.GREEN);
    public static final ILogType SQL = new BasicLogType("SQL", EnumLogColor.RED);
    public static final ILogType DISCORD_BOT = new BasicLogType("DISCORD_BOT", EnumLogColor.CYAN);
    public static final ILogType WEB_SERVER = new BasicLogType("WEB_SERVER", EnumLogColor.YELLOW);
    public static final ILogType STRIPE = new BasicLogType("STRIPE", EnumLogColor.BLUE);
}

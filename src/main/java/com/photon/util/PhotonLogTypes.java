package com.photon.util;

import niwer.lumen.EnumLogColor;
import niwer.lumen.types.BasicLogType;
import niwer.lumen.types.ILogType;

public class PhotonLogTypes {

    public static final ILogType TESTER = new BasicLogType("TESTER", EnumLogColor.BLACK); // Only used in Photon for testing purposes, not to be used in the final product

    public static final ILogType NETWORK = new BasicLogType("NETWORK", EnumLogColor.GREEN);
    public static final ILogType SQL = new BasicLogType("SQL", EnumLogColor.RED);
    public static final ILogType DISCORD_BOT = new BasicLogType("DISCORD_BOT", EnumLogColor.CYAN);
}

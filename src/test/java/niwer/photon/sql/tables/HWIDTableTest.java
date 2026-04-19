package niwer.photon.sql.tables;

public final class HWIDTableTest {

    private static String lastUserUUID;
    private static String lastHWID;
    private static String lastOperatingSystem;

    private HWIDTableTest() {}

    public static void reset() {
        lastUserUUID = null;
        lastHWID = null;
        lastOperatingSystem = null;
    }

    public static void save(String userUUID, String userHWID, String operatingSystem) {
        lastUserUUID = userUUID;
        lastHWID = userHWID;
        lastOperatingSystem = operatingSystem;
    }

    public static String lastUserUUID() { return lastUserUUID; }
    public static String lastHWID() { return lastHWID; }
    public static String lastOperatingSystem() { return lastOperatingSystem; }
}

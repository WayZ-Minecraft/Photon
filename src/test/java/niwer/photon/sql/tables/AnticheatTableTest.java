package niwer.photon.sql.tables;

public final class AnticheatTableTest {

    private static String lastUserUUID;
    private static String lastFileName;
    private static String lastFileMessage;
    private static String lastOperatingSystem;

    private AnticheatTableTest() {}

    public static void reset() {
        lastUserUUID = null;
        lastFileName = null;
        lastFileMessage = null;
        lastOperatingSystem = null;
    }

    public static void save(String userUUID, String fileName, String fileMessage, String operatingSystem) {
        lastUserUUID = userUUID;
        lastFileName = fileName;
        lastFileMessage = fileMessage;
        lastOperatingSystem = operatingSystem;
    }

    public static String lastUserUUID() { return lastUserUUID; }
    public static String lastFileName() { return lastFileName; }
    public static String lastFileMessage() { return lastFileMessage; }
    public static String lastOperatingSystem() { return lastOperatingSystem; }
}

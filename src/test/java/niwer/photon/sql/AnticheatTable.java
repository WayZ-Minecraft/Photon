package niwer.photon.sql;

public final class AnticheatTable {

    private static String lastUserUUID;
    private static String lastFileName;
    private static String lastFileMessage;
    private static String lastOperatingSystem;

    private AnticheatTable() {}

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
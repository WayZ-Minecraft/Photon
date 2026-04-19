package niwer.photon.sql.tables;

public final class CrashReportTableTest {

    private static String lastUserUUID;
    private static String lastFileName;
    private static String lastFileMessage;
    private static String lastSide;

    private CrashReportTableTest() {}

    public static void reset() {
        lastUserUUID = null;
        lastFileName = null;
        lastFileMessage = null;
        lastSide = null;
    }

    public static void save(String userUUID, String fileName, String fileMessage, String side) {
        lastUserUUID = userUUID;
        lastFileName = fileName;
        lastFileMessage = fileMessage;
        lastSide = side;
    }

    public static String lastUserUUID() { return lastUserUUID; }
    public static String lastFileName() { return lastFileName; }
    public static String lastFileMessage() { return lastFileMessage; }
    public static String lastSide() { return lastSide; }
}

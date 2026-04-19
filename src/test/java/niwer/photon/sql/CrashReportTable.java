package niwer.photon.sql;

public final class CrashReportTable {

    private static String lastUserUUID;
    private static String lastFileName;
    private static String lastFileMessage;

    private CrashReportTable() {}

    public static void reset() {
        lastUserUUID = null;
        lastFileName = null;
        lastFileMessage = null;
    }

    public static void save(String userUUID, String fileName, String fileMessage) {
        lastUserUUID = userUUID;
        lastFileName = fileName;
        lastFileMessage = fileMessage;
    }

    public static String lastUserUUID() { return lastUserUUID; }
    public static String lastFileName() { return lastFileName; }
    public static String lastFileMessage() { return lastFileMessage; }
}
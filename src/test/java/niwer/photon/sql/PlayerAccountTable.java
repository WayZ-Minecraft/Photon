package niwer.photon.sql;

public final class PlayerAccountTable {

    private static boolean emailExistsResult;
    private static String lastEmailChecked;

    private PlayerAccountTable() {}

    public static void reset() {
        emailExistsResult = false;
        lastEmailChecked = null;
    }

    public static void setEmailExistsResult(boolean result) {
        emailExistsResult = result;
    }

    public static boolean emailExists(String email) {
        lastEmailChecked = email;
        return emailExistsResult;
    }

    public static String lastEmailChecked() { return lastEmailChecked; }
}
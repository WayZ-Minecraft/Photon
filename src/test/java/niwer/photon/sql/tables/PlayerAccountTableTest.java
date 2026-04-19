package niwer.photon.sql.tables;

public final class PlayerAccountTableTest {

    private static boolean emailExistsResult;
    private static String lastEmailChecked;

    private PlayerAccountTableTest() {}

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

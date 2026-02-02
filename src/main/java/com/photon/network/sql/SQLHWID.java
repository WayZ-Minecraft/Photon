package com.photon.network.sql;

import com.photon.util.NetworkOnly;

@NetworkOnly
public class SQLHWID extends SQLInteraction {

    /**
     * Create HWID table with columns: userName, userUUID, userHWID, operatingSystem.
     */
    @Override
    public void register() {
        executeSQLCommand("CREATE TABLE IF NOT EXISTS HWID (userName TEXT NOT NULL, userUUID TEXT NOT NULL, userHWID TEXT NOT NULL, operatingSystem TEXT NOT NULL, PRIMARY KEY (userUUID, userHWID));");
    }

    /**
     * Add or update a HWID entry in the database.
     * 
     * @param userName The username
     * @param userUUID The user UUID
     * @param userHWID The hardware ID
     * @param operatingSystem The operating system
     */
    public static void save(String userName, String userUUID, String userHWID, String operatingSystem) {
        executeSQLCommand("INSERT OR REPLACE INTO HWID (userName, userUUID, userHWID, operatingSystem) VALUES (?, ?, ?, ?)", userName, userUUID, userHWID, operatingSystem);
    }

    /**
     * Check if a HWID exists for a given user.
     * 
     * @param userUUID The user UUID
     * @return true if HWID exists
     */
    public static boolean exist(String userUUID) {
        final String HWID = getHWID(userUUID);
        return HWID != null && !HWID.isEmpty();
    }

    /**
     * Get HWID for a specific user.
     * 
     * @param userUUID The user UUID
     * @return The hardware ID or null if not found
     */
    public static String getHWID(String userUUID) {
        return (String)executeSQLCommandForPrimitive("SELECT userHWID FROM HWID WHERE userUUID = ? LIMIT 1", userUUID);
    }

    /**
     * Delete a HWID entry.
     * 
     * @param userUUID The user UUID
     * @param userHWID The hardware ID
     */
    public static void deleteHWID(String userUUID, String userHWID) {
        executeSQLCommand("DELETE FROM HWID WHERE userUUID = ? AND userHWID = ?", userUUID, userHWID);
    }
}
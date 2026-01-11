package com.photon.network.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.photon.util.ConsoleManager;

public class SQLHWID extends SqlInteract {

    /**
     * Add or update a HWID entry in the database.
     * 
     * @param userName The username
     * @param userUUID The user UUID
     * @param userHWID The hardware ID
     * @param operatingSystem The operating system
     */
    public static void saveHWID(String userName, String userUUID, String userHWID, String operatingSystem) {
        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement(
                "INSERT OR REPLACE INTO HWID (userName, userUUID, userHWID, operatingSystem) VALUES (?, ?, ?, ?)");
            statement.setString(1, userName);
            statement.setString(2, userUUID);
            statement.setString(3, userHWID);
            statement.setString(4, operatingSystem);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (reconnect())
                saveHWID(userName, userUUID, userHWID, operatingSystem);
            else
                ConsoleManager.create("Error saving HWID for " + userUUID + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Check if a HWID exists for a given user.
     * 
     * @param userUUID The user UUID
     * @param userHWID The hardware ID
     * @return true if HWID exists
     */
    public static boolean hwidExists(String userUUID, String userHWID) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT COUNT(*) FROM HWID WHERE userUUID = ? AND userHWID = ?");
            statement.setString(1, userUUID);
            statement.setString(2, userHWID);
            result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            if (reconnect())
                return hwidExists(userUUID, userHWID);
            else {
                ConsoleManager.create("Error checking HWID for " + userUUID + ": " + e.getMessage()).error().end();
                return false;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Get HWID for a specific user.
     * 
     * @param userUUID The user UUID
     * @return The hardware ID or null if not found
     */
    public static String getHWID(String userUUID) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT userHWID FROM HWID WHERE userUUID = ? LIMIT 1");
            statement.setString(1, userUUID);
            result = statement.executeQuery();

            if (result.next()) {
                return result.getString("userHWID");
            }
            return null;

        } catch (SQLException e) {
            if (reconnect())
                return getHWID(userUUID);
            else {
                ConsoleManager.create("Error getting HWID for " + userUUID + ": " + e.getMessage()).error().end();
                return null;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Delete a HWID entry.
     * 
     * @param userUUID The user UUID
     * @param userHWID The hardware ID
     */
    public static void deleteHWID(String userUUID, String userHWID) {
        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement("DELETE FROM HWID WHERE userUUID = ? AND userHWID = ?");
            statement.setString(1, userUUID);
            statement.setString(2, userHWID);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (reconnect())
                deleteHWID(userUUID, userHWID);
            else
                ConsoleManager.create("Error deleting HWID for " + userUUID + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }
}
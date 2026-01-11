package com.photon.network.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.photon.util.ConsoleManager;

public class SQLCrashReport extends SqlInteract {

    /**
     * Save a crash report to the database.
     * 
     * @param userUUID The user UUID
     * @param fileName The file name
     * @param fileMessage The crash report message
     */
    public static void saveCrashReport(String userUUID, String fileName, String fileMessage) {
        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement(
                "INSERT INTO CrashReport (userUUID, fileName, fileMessage) VALUES (?, ?, ?)");
            statement.setString(1, userUUID);
            statement.setString(2, fileName);
            statement.setString(3, fileMessage);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (reconnect())
                saveCrashReport(userUUID, fileName, fileMessage);
            else
                ConsoleManager.create("Error saving crash report for " + userUUID + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }
}
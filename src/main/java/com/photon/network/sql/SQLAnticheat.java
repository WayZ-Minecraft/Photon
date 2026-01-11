package com.photon.network.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.photon.util.ConsoleManager;

public class SQLAnticheat extends SqlInteract {

    /**
     * @param userUUID The user UUID
     * @param fileName The file name
     * @param fileMessage The cheat detection message
     * @param operatingSystem The operating system
     */
    public static void saveAnticheatReport(String userUUID, String fileName, String fileMessage, String operatingSystem) {
        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement(
                "INSERT INTO Anticheat (userUUID, fileName, fileMessage, operatingSystem) VALUES (?, ?, ?, ?)");
            statement.setString(1, userUUID);
            statement.setString(2, fileName);
            statement.setString(3, fileMessage);
            statement.setString(4, operatingSystem);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (reconnect())
                saveAnticheatReport(userUUID, fileName, fileMessage, operatingSystem);
            else
                ConsoleManager.create("Error saving anticheat report for " + userUUID + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }
}
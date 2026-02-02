package com.photon.network.sql;

import com.photon.util.NetworkOnly;

@NetworkOnly
public class SQLAnticheat extends SQLInteraction {

    /**
     * Create Anticheat table with columns: id, userUUID, fileName, fileMessage, operatingSystem, timestamp.
     */
    @Override
    public void register() {
        executeSQLCommand("CREATE TABLE IF NOT EXISTS Anticheat (id INTEGER PRIMARY KEY AUTOINCREMENT, userUUID TEXT NOT NULL, fileName TEXT NOT NULL, fileMessage TEXT NOT NULL, operatingSystem TEXT NOT NULL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);");
    }

    /**
     * @param userUUID The user UUID
     * @param fileName The file name
     * @param fileMessage The cheat detection message
     * @param operatingSystem The operating system
     */
    public static void save(String userUUID, String fileName, String fileMessage, String operatingSystem) {
        executeSQLCommand("INSERT INTO Anticheat (userUUID, fileName, fileMessage, operatingSystem) VALUES (?, ?, ?, ?)",
            userUUID, fileName, fileMessage, operatingSystem
        );
    }
}
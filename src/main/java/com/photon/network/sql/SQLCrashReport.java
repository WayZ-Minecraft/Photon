package com.photon.network.sql;

import com.photon.util.NetworkOnly;

@NetworkOnly
public class SQLCrashReport extends SQLInteraction {

    /**
     * Create CrashReport table with columns: id, userUUID, fileName, fileMessage, timestamp.
     */
    @Override
    public void register() {
        executeSQLCommand("CREATE TABLE IF NOT EXISTS CrashReport (id INTEGER PRIMARY KEY AUTOINCREMENT, userUUID TEXT NOT NULL, fileName TEXT NOT NULL, fileMessage TEXT NOT NULL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);");
    }

    /**
     * Save a crash report to the database.
     * 
     * @param userUUID The user UUID
     * @param fileName The file name
     * @param fileMessage The crash report message
     */
    public static void save(String userUUID, String fileName, String fileMessage) {
        executeSQLCommand("INSERT INTO CrashReport (userUUID, fileName, fileMessage) VALUES (?, ?, ?)",
            userUUID, fileName, fileMessage
        );
    }
}
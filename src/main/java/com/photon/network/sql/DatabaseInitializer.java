package com.photon.network.sql;

import java.sql.SQLException;
import java.sql.Statement;

import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * Database initialization handler for SQLite tables.
 * Creates and populates all required tables on first connection.
 * 
 * @author noz43
 * @version 1.0
 */
public class DatabaseInitializer extends SqlInteract {

    /**
     * Initialize all database tables and populate with default data.
     */
    public static void initializeTables() {
        createUserTable();
        createNewsTable();
        createLevelTable();
        populateLevelTable();
    }

    /**
     * Create User table with columns: id, xp, level, languages, firstConnection.
     */
    private static void createUserTable() {
        Statement statement = null;
        try {
            statement = connexion.createStatement();
            
            String sql = "CREATE TABLE IF NOT EXISTS User (" +
                        "id TEXT PRIMARY KEY NOT NULL, " +
                        "xp INTEGER DEFAULT 0, " +
                        "level INTEGER DEFAULT 1, " +
                        "languages TEXT, " +
                        "firstConnection INTEGER DEFAULT 1" +
                        ");";
            
            statement.executeUpdate(sql);
            ConsoleManager.create("Table User initialized").withType(EnumLogType.NETWORK).end();
            
        } catch (SQLException e) {
            ConsoleManager.create("Error creating User table: " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Create News table with columns: id, title, contentEn, contentFr, date, imagepath.
     */
    private static void createNewsTable() {
        Statement statement = null;
        try {
            statement = connexion.createStatement();
            
            String sql = "CREATE TABLE IF NOT EXISTS News (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "contentEn TEXT, " +
                        "contentFr TEXT, " +
                        "date DATE NOT NULL, " +
                        "imagepath TEXT" +
                        ");";
            
            statement.executeUpdate(sql);
            ConsoleManager.create("Table News initialized").withType(EnumLogType.NETWORK).end();
            
        } catch (SQLException e) {
            ConsoleManager.create("Error creating News table: " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Create Level table with columns: number, xpLevel.
     */
    private static void createLevelTable() {
        Statement statement = null;
        try {
            statement = connexion.createStatement();
            
            String sql = "CREATE TABLE IF NOT EXISTS Level (" +
                        "number INTEGER PRIMARY KEY NOT NULL, " +
                        "xpLevel INTEGER NOT NULL" +
                        ");";
            
            statement.executeUpdate(sql);
            ConsoleManager.create("Table Level initialized").withType(EnumLogType.NETWORK).end();
            
        } catch (SQLException e) {
            ConsoleManager.create("Error creating Level table: " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Populate Level table with XP requirements for levels 1-100.
     * Formula: XP = 100 * 1.5^(level-1)
     */
    private static void populateLevelTable() {
        Statement statement = null;
        try {
            statement = connexion.createStatement();
            
            for (int level = 1; level <= 100; level++) {
                int xpRequired = calculateXpForLevel(level);
                String sql = "INSERT OR IGNORE INTO Level (number, xpLevel) VALUES (" + level + ", " + xpRequired + ");";
                statement.executeUpdate(sql);
            }
            
            ConsoleManager.create("Level table populated with 100 levels").withType(EnumLogType.NETWORK).end();
            
        } catch (SQLException e) {
            ConsoleManager.create("Error populating Level table: " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Calculate XP requirement for a given level.
     * 
     * @param level The level number
     * @return Required XP amount
     */
    private static int calculateXpForLevel(int level) {
        return (int) (100 * Math.pow(1.5, level - 1));
    }
}
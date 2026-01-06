package com.photon.network.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * SQLite database interaction handler.
 * Manages connection, reconnection and basic database operations.
 * 
 * @author noz43
 * @version 1.0
 */
public class SqlInteract {
    protected static Connection connexion = null;
    
    /**
     * Establish connection to SQLite database and initialize tables.
     */
    public static void connect() {
        try {
            String url = "jdbc:sqlite:" + NetworkDirectories.sqlDirectory + "/main.db";
            connexion = DriverManager.getConnection(url);
            
            ConsoleManager.create("Connection to SQLite has been established.").withType(EnumLogType.NETWORK).end();
            
            DatabaseInit.initializeTables();
            
        } catch (SQLException e) {
            ConsoleManager.create("Database connection error: " + e.getMessage()).error().end();
        }
    }

    /**
     * Close statement and result set safely.
     * 
     * @param statement The statement to close
     * @param resultat The result set to close
     */
    protected static void closeStatement(Statement statement, ResultSet resultat) {
        try {
            if (statement != null) statement.close();
            if (resultat != null) resultat.close();
        } catch (SQLException ignore) {
        }
    }

    /**
     * Reconnect to database if connection is null.
     * 
     * @return true if reconnection was needed, false otherwise
     */
    protected static boolean reconnect() {
        if (connexion == null) {
            connect();
            return true;
        }
        return false;
    }

    /**
     * Add a new user to the database with default values.
     * 
     * @param id The discord id of the user
     */
    public static void addUser(String id) {
        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement("INSERT INTO User (id, xp) VALUES (?, 0)");
            statement.setString(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (reconnect())
                addUser(id);
            else
                ConsoleManager.create("Error adding user " + id + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Execute a custom SQL command and return results as formatted string.
     * 
     * WARNING: This method should NEVER be called with user input!
     * Only use with hardcoded queries or admin-only commands.
     * Use PreparedStatement methods in other SQL classes for user-facing queries.
     * 
     * @param command The SQL command to execute
     * @return Formatted result string with columns separated by " | "
     * @throws SQLException if query execution fails
     */
    public static String commandSql(String command) throws SQLException {
        Statement statement = connexion.createStatement();
        ResultSet result = statement.executeQuery(command);

        int columnCount = result.getMetaData().getColumnCount();
        String resultString = "";
        while(result.next()) {
            for (int i = 1; i <= columnCount; i++) {
                resultString += result.getString(i);
                resultString += (i != columnCount) ? " | " : "\n";
            }
        }

        closeStatement(statement, result);
        return resultString;
    }
}
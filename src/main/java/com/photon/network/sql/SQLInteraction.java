package com.photon.network.sql;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.NetworkOnly;

/**
 * SQLite database interaction handler.
 * Manages connection, reconnection and basic database operations.
 * 
 * @author noz43
 * @version 1.0
 */
@NetworkOnly
public abstract class SQLInteraction {
    protected static Connection connexion = null;
    
    /**
     * Register the SQL tables and prepare statements.
     */
    public abstract void register();

    /**
     * Establish connection to SQLite database and initialize tables.
     */
    public static void connect() {
        try {
            final String DB_PATH = "jdbc:sqlite:" + NetworkDirectories.baseDirectory + "/network.db";
            connexion = DriverManager.getConnection(DB_PATH);
            ConsoleManager.create("Connection to SQLite has been established.").withType(EnumLogType.NETWORK).end();
            
            /* Register all SQL interactions */
            {
                new SQLnews().register();

                /* Security */
                new SQLAnticheat().register();
                new SQLHWID().register();
                new SQLCrashReport().register();

                /* User Accounts */
                new SQLPlayerAccount().register();
                new SQLDiscordProfile().register();
            }
        } catch (SQLException e) {
            ConsoleManager.create("Database connection error: " + e.getMessage()).withType(EnumLogType.SQL).error().end();
        }
    }

    /**
     * Close statement and result set safely.
     * 
     * @param statement The statement to close
     * @param result The result set to close
     */
    protected static void closeStatement(Statement statement, ResultSet result) {
        try {
            if (statement != null) statement.close();
            if (result != null) result.close();
        } catch (SQLException ignore) {}
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
     * This command will execute an SQL command and return the result as a formatted string.
     * @param command The SQL command to execute
     * @return Formatted result string with columns separated by " | "
     * @throws SQLException if query execution fails
     */
    public static String executeSQLCommandToArray(String command, boolean markdown) throws SQLException {
        final Statement STATEMENT = connexion.createStatement();
        final ResultSet RESULT = STATEMENT.executeQuery(command);
        final int COLUMN_COUNT = RESULT.getMetaData().getColumnCount();
        final int[] COLUMNS_WIDTHS = new int[COLUMN_COUNT]; // E.G : [5, 10, 3] for 3 columns
        final StringBuilder BUILDER = new StringBuilder();
        final List<String[]> ROWS = new ArrayList<>(); // List of rows, each row is an array of strings (The array represents the fields for each column)
        
        /* Calculate column size and save create rows with values for each column */
        for (int columnID = 0; columnID < COLUMN_COUNT; columnID++) {
            String columnName = RESULT.getMetaData().getColumnName(columnID + 1);
            if (columnName == null) columnName = "";
            if (columnName.length() > COLUMNS_WIDTHS[columnID]) COLUMNS_WIDTHS[columnID] = columnName.length();
        }
        while (RESULT.next()) {
            String[] row = new String[COLUMN_COUNT];
            for (int columnID = 0; columnID < COLUMN_COUNT; columnID++) {
                String field = RESULT.getString(columnID + 1);
                if (field == null) field = ""; // If null, set empty string
                
                row[columnID] = field; // Save field in the row
                if (field.length() > COLUMNS_WIDTHS[columnID]) COLUMNS_WIDTHS[columnID] = field.length(); // If the field is wider than the current width, set it
            }
            ROWS.add(row);
        }
        
        /* Create the board (if markdown is set to true, then we'll print with markdown formatting) */
        {
            if (markdown) BUILDER.append("```\n");
            /* Title row */
            BUILDER.append("| ");
            for (int i = 0; i < COLUMN_COUNT; i++) {
                String columnName = RESULT.getMetaData().getColumnName(i + 1);
                if (columnName == null) columnName = "";
                BUILDER.append(String.format("%-" + COLUMNS_WIDTHS[i] + "s", columnName));
                BUILDER.append(" | ");
            }
            BUILDER.append("\n|");
            for (int i = 0; i < COLUMN_COUNT; i++) BUILDER.append(" ").append("-".repeat(COLUMNS_WIDTHS[i])).append(" |");
            BUILDER.append("\n");
            
            /* Data rows */
            for (final String[] ROW : ROWS) {
                BUILDER.append("| ");
                for (int i = 0; i < COLUMN_COUNT; i++) {
                    final String VALUE = ROW[i];
                    BUILDER.append(String.format("%-" + COLUMNS_WIDTHS[i] + "s", VALUE)).append(" | ");
                }
                BUILDER.append("\n");
            }
            if (markdown) BUILDER.append("```");
        }
        
        closeStatement(STATEMENT, RESULT);
        return new String(BUILDER.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    /**
     * Execute an SQL command without expecting a return value.
     * @param command The SQL command to execute
     * @param params The parameters to set in the prepared statement
     * @author Niwer
     */
    public static void executeSQLCommand(String command, Object... params) { executeSQLCommand(null, command, params); }

    /**
     * @param command The SQL command to execute
     * @param params  The parameters to set in the prepared statement
     * @return The objectified result of type T, or null if no result
     * @author Niwer
     */
    public static <T extends SQLCommandSerializer<T>> T executeSQLCommand(Class<T> serializer, String command, Object... params) {
        try {
            final PreparedStatement statement = connexion.prepareStatement(command);

            /* Pass all objects to the command */
            for (int i = 0; i < params.length; i++) statement.setObject(i + 1, params[i]);

            /* If no serializer is provided, execute without return */
            if(serializer == null) {
                statement.executeUpdate();
                closeStatement(statement, null);
                return null;
            }

            /* Execture and save the result */
            T obj = null; // Object to return
            final ResultSet RESULT = statement.executeQuery();

            /* Convert the result and return it */
            if (RESULT.next()) {
                obj = (T) serializer.getDeclaredConstructor().newInstance();
                obj = obj.objectify(RESULT);
            }
            closeStatement(statement, RESULT);
            return obj;
        } catch (SQLException e) {
            if (reconnect()) return executeSQLCommand(serializer, command, params);
            ConsoleManager.create(String.format("Error while executing SQL command (%s) : ", command) + e.getMessage()).withType(EnumLogType.SQL).error().end();
            return null;
        } catch (Exception e) {
            ConsoleManager.create("Error while serializing : " + e.getMessage()).withType(EnumLogType.SQL).error().end();
            return null;
        }
    }

    public static <T extends SQLCommandSerializer<T>> List<T> executeSQLCommandList(Class<T> serializer, String command, Object... params) {
        try {
            final PreparedStatement statement = connexion.prepareStatement(command);

            /* Pass all objects to the command */
            for (int i = 0; i < params.length; i++) statement.setObject(i + 1, params[i]);

            /* Execture and save the result */
            final ResultSet RESULT = statement.executeQuery();
            final java.util.List<T> list = new java.util.ArrayList<>();

            /* Convert the result and return it */
            while (RESULT.next()) {
                T obj = (T) serializer.getDeclaredConstructor().newInstance();
                obj = obj.objectify(RESULT);
                list.add(obj);
            }
            closeStatement(statement, RESULT);
            return list;
        } catch (SQLException e) {
            if (reconnect()) return executeSQLCommandList(serializer, command, params);
            ConsoleManager.create(String.format("Error while executing SQL command (%s) : ", command) + e.getMessage()).withType(EnumLogType.SQL).error().end();
            return null;
        } catch (Exception e) {
            ConsoleManager.create("Error while serializing : " + e.getMessage()).withType(EnumLogType.SQL).error().end();
            return null;
        }
    }

    /**
     * Execute an SQL command and return a single value of a default Java type.
     * @param command The SQL command to execute
     * @param params The parameters to set in the prepared statement
     * @return The result as an Object, or null if no result
     */
    public static Object executeSQLCommandForPrimitive(String command, Object... params) {
        try {
            final PreparedStatement statement = connexion.prepareStatement(command);

            /* Pass all objects to the command */
            for (int i = 0; i < params.length; i++) statement.setObject(i + 1, params[i]);

            final ResultSet RESULT = statement.executeQuery();

            /* If there is a result, return the first column of the first row */
            if (RESULT.next()) {
                Object result = RESULT.getObject(1);
                closeStatement(statement, RESULT);
                return result;
            }
            closeStatement(statement, RESULT);
            return null;
        } catch (SQLException e) {
            if (reconnect()) return executeSQLCommandForPrimitive(command, params);
            ConsoleManager.create(String.format("Error while executing SQL command (%s) : ", command) + e.getMessage()).withType(EnumLogType.SQL).error().end();
            return null;
        }
    }

    @NetworkOnly
    public static interface SQLCommandSerializer<T> {
        /**
         * This method will convert a ResultSet row into an object of type T.
         * @param resultSet The ResultSet to objectify
         * @return The object of type T
         * @throws SQLException if an SQL error occurs
         */
        T objectify(ResultSet resultSet) throws SQLException;
    }
}
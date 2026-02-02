package com.photon.network.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    public static String executeSQLCommandToArray(String command) throws SQLException {
        final Statement STATEMENT = connexion.createStatement();
        final ResultSet RESULT = STATEMENT.executeQuery(command);
        final int COLUMN_COUNT = RESULT.getMetaData().getColumnCount();

        String resultString = "";
        while(RESULT.next()) {
            for (int i = 1; i <= COLUMN_COUNT; i++) {
                resultString += RESULT.getString(i);
                resultString += (i != COLUMN_COUNT) ? " | " : "\n";
            }
        }

        closeStatement(STATEMENT, RESULT);
        return resultString;
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
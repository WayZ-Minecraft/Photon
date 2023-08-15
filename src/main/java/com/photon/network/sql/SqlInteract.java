package com.photon.network.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;


public class SqlInteract {
     /**
     * Connect to a sample database
     */
    protected static Connection connexion = null;
    public static void connect() {

        try {
            // db parameters
            String url = "jdbc:sqlite:"+NetworkDirectories.sqlDirectory+"/main.db";
            // create a connection to the database
            connexion = DriverManager.getConnection(url);
            
            ConsoleManager.create("Connection to SQLite has been established.").withType(EnumLogType.NETWORK).end();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    protected static void closeStatement(Statement statement, ResultSet resultat) {
        try {
            if (statement != null) statement.close();
            if (resultat != null) resultat.close();
        } catch (SQLException ignore) {
        }
    }

    /**
     * If the connection is closed, reconnect
     * @return true if the connection was closed
     */
    protected static boolean reconnect() {
        if (connexion == null) {
            connect();
            return true;
        }
        return false;
    }

    /**
     * Add a user to the database
     * 
     * @param id the discord id of the user
     */
    public static void addUser(String id) {

        Statement statement = null;

        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            statement.executeUpdate("INSERT INTO User (id, xp) VALUES ('" + id + "', 0);");

        } catch (SQLException e) {
            if (reconnect())
                addUser(id);
            else
                ConsoleManager.create("Erreur on add a new User : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexionJ
            closeStatement(statement, null);
        }
    }

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


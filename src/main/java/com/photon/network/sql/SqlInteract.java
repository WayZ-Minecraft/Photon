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

    protected static void reconnect() {
        if (connexion != null) {
            try {
                connexion.close();
            } catch (SQLException ignore) {
            }
        }
        connect();
    }

    


}


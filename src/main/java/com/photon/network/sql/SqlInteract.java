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

    public static void testTime(){
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            try {
                SQLxp.getXp("");
                
            } catch (Exception ignore) {}
        }

        long endTime = System.currentTimeMillis();
        ConsoleManager.create("That took " + (endTime - startTime) + " milliseconds").withType(EnumLogType.NETWORK).end();
    }

    


}


package com.photon.network.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.photon.network.NetworkDirectories;
import com.photon.network.sql.customExeption.LevelNotFoundExepction;
import com.photon.network.sql.customExeption.PlayerNotFoundException;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;


public class SqlInteract {
     /**
     * Connect to a sample database
     */
    private static Connection connexion = null;
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

    private static void closeStatement(Statement statement, ResultSet resultat) {
        try {
            if (statement != null) statement.close();
            if (resultat != null) resultat.close();
        } catch (SQLException ignore) {
        }
    }

    /**
     * Give a list of the xp leaderboard
     * @param top the number of user to get
     * @return List<String[]> : the list of the user id and xp [id, level, xp]
     */
    public static List<String[]> getLeaderboard(int top) {

        ArrayList<String[]> leadboard = new ArrayList<>();

        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            resultat = statement.executeQuery("SELECT id, xp, level FROM User ORDER BY level DESC, xp DESC LIMIT "+top+";");

            // Récupération des données
            while (resultat.next()) {
                String id = resultat.getString("id");
                String level = resultat.getString("level");
                String xp = resultat.getString("xp");
                
                String[] user = {id, level, xp};
                leadboard.add(user);
            }
        } catch (SQLException e) {
            ConsoleManager.create("Erreur on geting leaderboad : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }

        return leadboard;
   
    }

    /**
     * Add a user to the database
     * @param id the discord id of the user
     */
    public static void addUser(String id){

        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            statement.executeUpdate("INSERT INTO User (id, xp) VALUES ('"+id+"', 0);");

        } catch (SQLException e) {
            ConsoleManager.create("Erreur on add a new User : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexionJ
            closeStatement(statement, resultat);
        }
    }


    /**
     * set the xp of a user
     * @param id the discord id of the user
     * @param number the new xp of the user
     */
    public static void setXp(String id, int number){
            
        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            int nb = statement.executeUpdate("UPDATE User SET xp = "+number+" WHERE id = '"+id+"';");

            if (nb == 0) throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            setXp(id, number);
        } catch (SQLException e) {
            ConsoleManager.create("Erreur on set xp to "+id+" : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }
    }


    /**
     * add xp to a user
     * @param id the discord id of the user
     */
    public static void addXp(String id, int number){
            
        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();


            // Exécution de la requête
            int nb = statement.executeUpdate("UPDATE User SET xp = xp + "+number+" WHERE id = '"+id+"';");

            if (nb == 0) throw new PlayerNotFoundException("User not found");
            

        } catch (PlayerNotFoundException e) {
            addUser(id);
            addXp(id, number);
        } catch (SQLException e) {
            ConsoleManager.create("Erreur on add xp to "+id+" : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }
    }

    /**
     * Get the level of a user
     * @param id the discord id of the user
     * @return int : the level of the user
     */
    public static int getLevel(String id){
            
        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            resultat = statement.executeQuery("SELECT level FROM User WHERE id = '"+id+"';");

            // Récupération des données
            if (resultat.next()) return resultat.getInt("level");
            else throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            return 0;
        } catch (SQLException e) {
            ConsoleManager.create("Erreur on get level of "+id+" : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }

        return 0;
    }

    /**
     * set the level of a user
     * @param id the discord id of the user
     * @param level the new level of the user
     */
    public static void setLevel(String id, int level){
            
        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            int nb = statement.executeUpdate("UPDATE User SET level = "+level+" WHERE id = '"+id+"';");

            if (nb == 0) throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            setLevel(id, level);
        } catch (SQLException e) {
            ConsoleManager.create("Erreur on set level to "+id+" : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }
    }


    /**
     * add a level to a user
     * @param id the discord id of the user
     * @param level the number of level to add
     */
    public static void addLevel(String id, int level){
            
        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            int nb = statement.executeUpdate("UPDATE User SET level = level + "+level+" WHERE id = '"+id+"';");

            if (nb == 0) throw new PlayerNotFoundException("User not found");
            

        } catch (PlayerNotFoundException e) {
            addUser(id);
        } catch (SQLException e) {
            ConsoleManager.create("Erreur on add level to "+id+" : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }
    }

    /**
     * Get the xp to pass to the next level
     * @param level the level of the user
     * @return int : the xp to pass to the next level
     */
    public static int getXpLevel(int level){
        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            resultat = statement.executeQuery("SELECT xpLevel FROM Level WHERE number = '"+level+"';");

            // Récupération des données
            if (resultat.next()) return resultat.getInt("xpLevel");
            else throw new LevelNotFoundExepction("Level not found");

        } catch (LevelNotFoundExepction e) {
            ConsoleManager.create("Erreur on get xp level of "+level+" : " + e.getMessage()).error().displayOnDiscord().end();
            return 5000;
        } catch (SQLException e) {
            ConsoleManager.create("Erreur on get xp level of "+level+" : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }

        return 0;
    }

    /**
     * Get the xp of a user
     * @param id the discord id of the user
     * @return int : the xp of the user
     */
    public static int getXp(String id){

        Statement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            resultat = statement.executeQuery("SELECT xp FROM User WHERE id = '"+id+"';");

            // Récupération des données
            if (resultat.next()) return resultat.getInt("xp");
            else throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            return 0;
        } catch (SQLException e) {
            ConsoleManager.create("Erreur on get xp of "+id+" : " + e.getMessage()).error().end();
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }

        return 0;

    }


    /**
     * Get the xp to pass to the next level
     * @param id the discord id of the user
     * @return int : the xp to pass to the next level
     * 
     * @note equivalent to getXpLevel(getLevel(id)) but use less request
     */
    public static int getXpToNextLevel(String id) throws LevelNotFoundExepction{
        
        Statement statement = null;
        ResultSet resultat = null;

        try {
            statement = connexion.createStatement();

            // Exécution de la requête
            resultat = statement.executeQuery("SELECT xpLevel FROM Level WHERE number = (SELECT level FROM User WHERE id = '"+id+"');");

            // Récupération des données
            if (resultat.next()) return resultat.getInt("xpLevel");
            else throw new LevelNotFoundExepction("Level not found");

        } catch (SQLException e) {
            ConsoleManager.create("Erreur on get xp of "+id+" : " + e.getMessage()).error().end();
            connect();
            return getXpToNextLevel(id);
        } finally {
            // Fermeture de la connexion
            closeStatement(statement, resultat);
        }
    }

    /**
     * Get the rank of a user
     * @param id the discord id of the user
     * @return int : the rank of the user
     */
    public static int getRank(String id){
            
            Statement statement = null;
            ResultSet resultat = null;
            
            try {
                statement = connexion.createStatement();
    
                // Exécution de la requête
                resultat = statement.executeQuery("SELECT COUNT(*)  FROM User WHERE level > (SELECT level FROM User WHERE id = '"+id+"') OR (level = (SELECT level FROM User WHERE id = '"+id+"') AND xp >= (SELECT xp FROM User WHERE id = '"+id+"') );");
    
                // Récupération des données
                int rank = 0;
                if (resultat.next()) rank = resultat.getInt("COUNT(*)");

                if (rank == 0) throw new PlayerNotFoundException("User not found");
                return rank;
    
            } catch (SQLException e) {
                ConsoleManager.create("Erreur on get rank of "+id+" : " + e.getMessage()).error().end();
                connect();
                return getRank(id);
            } catch (PlayerNotFoundException e) {
                addUser(id);
                return getRank(id);

            } finally {
                // Fermeture de la connexion
                closeStatement(statement, resultat);
            }
    
    }


}


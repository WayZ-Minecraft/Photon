package com.photon.network.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.photon.discord.usersInteraction.language.Languages;
import com.photon.network.sql.customExeption.PlayerNotFoundException;

public class SQLuser extends SqlInteract {

    static Gson  gson = new GsonBuilder().create();
    
    /**
     * Get an array of the languages of a user
     * @param id the id of the user
     * @return an array of the languages of a user
     * @throws SQLException
     */
    public static ArrayList<Languages> getLanguages(String id) throws SQLException{

        Statement statement = connexion.createStatement();
        ResultSet result = statement.executeQuery("SELECT languages FROM User WHERE id = '" + id + "'");

        TypeToken<ArrayList<Languages>> typeToken = new TypeToken<ArrayList<Languages>>() {};

        try {
            ArrayList<Languages> languages = null;
            if(result.next()){
                String languagesString = result.getString("languages");
                if (languagesString != null)
                    languages = gson.fromJson(languagesString, typeToken.getType());
            }
            else throw new PlayerNotFoundException("User not found");

            return languages;
        } catch (PlayerNotFoundException e) {
            addUser(id);
            return null;
        } finally {
            closeStatement(statement, result);
        }

    }

    /**
     * Set the languages of a user
     * @param id the id of the user
     * @param language the language to add
     * @throws SQLException
     */
    public static void setLanguages(String id, ArrayList<Languages> languages) throws SQLException{

        Statement statement = connexion.createStatement();
        try {
            int nb = statement.executeUpdate("UPDATE User SET languages = '" + gson.toJson(languages) + "' WHERE id = '" + id + "'");
    
            if(nb == 0) throw new PlayerNotFoundException("User not found");
            
        } catch (PlayerNotFoundException e) {
            addUser(id);
            setLanguages(id, languages);
        } finally {
            closeStatement(statement, null);
        }
        
    }

    /**
     * is the first connection of the user
     * @param id the id of the user
     * @return true if it's the first connection, false if not
     */
    public static boolean getFirstConnection(String id) {
        Statement statement = null;
        ResultSet result = null;
        try {
            statement = connexion.createStatement();
            result = statement.executeQuery("SELECT firstConnection FROM User WHERE id = '" + id + "'");
            if(result.next()){
                return result.getBoolean("firstConnection");
            }
            else throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement, result);
        }

    }

    /**
     * Set the first connection of the user
     * @param id the id of the user
     * @param firstConnection the first connection of the user
     * @throws SQLException
     */
    public static void setFirstConnection(String id, boolean firstConnection) throws SQLException{

        Statement statement = connexion.createStatement();
        try {
            int nb = statement.executeUpdate("UPDATE User SET firstConnection = '" + firstConnection + "' WHERE id = '" + id + "'");
    
            if(nb == 0) throw new PlayerNotFoundException("User not found");
            
        } catch (PlayerNotFoundException e) {
            addUser(id);
            setFirstConnection(id, firstConnection);
        } finally {
            closeStatement(statement, null);
        }
    }

}

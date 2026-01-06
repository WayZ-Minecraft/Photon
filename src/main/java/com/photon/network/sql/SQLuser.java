package com.photon.network.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.photon.discord.usersInteraction.language.Languages;
import com.photon.network.sql.exceptions.PlayerNotFoundException;
import com.photon.util.ConsoleManager;

public class SQLuser extends SqlInteract {

    static Gson gson = new GsonBuilder().create();
    
    /**
     * Retrieve language preferences for a user.
     * 
     * @param id The discord id of the user
     * @return List of Languages or null if user has no preferences
     * @throws SQLException if query execution fails
     */
    public static ArrayList<Languages> getLanguages(String id) throws SQLException{
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT languages FROM User WHERE id = ?");
            statement.setString(1, id);
            result = statement.executeQuery();

            TypeToken<ArrayList<Languages>> typeToken = new TypeToken<ArrayList<Languages>>() {};

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
     * Update language preferences for a user.
     * 
     * @param id The discord id of the user
     * @param languages List of Languages to set
     * @throws SQLException if query execution fails
     */
    public static void setLanguages(String id, ArrayList<Languages> languages) throws SQLException{
        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement("UPDATE User SET languages = ? WHERE id = ?");
            statement.setString(1, gson.toJson(languages));
            statement.setString(2, id);
            
            int nb = statement.executeUpdate();
            if(nb == 0) throw new PlayerNotFoundException("User not found");
            
        } catch (PlayerNotFoundException e) {
            addUser(id);
            setLanguages(id, languages);
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Check if this is the user's first connection.
     * 
     * @param id The discord id of the user
     * @return true if first connection, false otherwise
     */
    public static boolean getFirstConnection(String id) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT firstConnection FROM User WHERE id = ?");
            statement.setString(1, id);
            result = statement.executeQuery();

            if(result.next()){
                return result.getBoolean("firstConnection");
            }
            else throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            return true;
        } catch (SQLException e) {
            ConsoleManager.create("Error getting first connection for " + id + ": " + e.getMessage()).error().end();
            return false;
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Update first connection status for a user.
     * 
     * @param id The discord id of the user
     * @param firstConnection The new status
     * @throws SQLException if query execution fails
     */
    public static void setFirstConnection(String id, boolean firstConnection) throws SQLException{
        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement("UPDATE User SET firstConnection = ? WHERE id = ?");
            statement.setInt(1, firstConnection ? 1 : 0);
            statement.setString(2, id);
            
            int nb = statement.executeUpdate();
            if(nb == 0) throw new PlayerNotFoundException("User not found");
            
        } catch (PlayerNotFoundException e) {
            addUser(id);
            setFirstConnection(id, firstConnection);
        } finally {
            closeStatement(statement, null);
        }
    }
}
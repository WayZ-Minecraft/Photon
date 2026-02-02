package com.photon.network.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.photon.discord.language.Languages;
import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager;
import com.photon.util.NetworkOnly;

//TODO fix this mess and use the newer language system
@NetworkOnly
public class SQLDiscordProfile extends SQLInteraction {
    
    /**
     * Create User table with columns: id, xp, level, languages, firstConnection.
     */
    @Override
    public void register() {
        executeSQLCommand("CREATE TABLE IF NOT EXISTS DiscordAccount (id TEXT PRIMARY KEY NOT NULL, xp INTEGER DEFAULT 0, level INTEGER DEFAULT 1, languages TEXT, firstConnection INTEGER DEFAULT 1);");
    }

    /**
     * Retrieve language preferences for a user.
     * 
     * @param id The discord id of the user
     * @return List of Languages or null if user has no preferences
     * @throws SQLException if query execution fails
     */
    public static ArrayList<Languages> getLanguages(String id) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT languages FROM DiscordAccount WHERE id = ?");
            statement.setString(1, id);
            result = statement.executeQuery();

            TypeToken<ArrayList<Languages>> typeToken = new TypeToken<ArrayList<Languages>>() {};

            ArrayList<Languages> languages = null;
            if(result.next()){
                String languagesString = result.getString("languages");
                if (languagesString != null)
                    languages = NetworkDirectories.GSON.fromJson(languagesString, typeToken.getType());
            }

            return languages;
        } catch (SQLException e) {
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
    public static void setLanguages(String id, List<Languages> languages) {
        executeSQLCommand("UPDATE DiscordAccount SET languages = ? WHERE id = ?", NetworkDirectories.GSON.toJson(languages), id);
    }

    /**
     * Check if this is the user's first connection.
     * 
     * @param id The discord id of the user
     * @return true if first connection, false otherwise
     */
    public static boolean isFirstConnection(String id) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT firstConnection FROM DiscordAccount WHERE id = ?");
            statement.setString(1, id);
            result = statement.executeQuery();

            if(result.next()){
                return result.getBoolean("firstConnection");
            }
        }catch (SQLException e) {
            ConsoleManager.create("Error getting first connection for " + id + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, result);
        }
        return false;
    }

    /**
     * Update first connection status for a user.
     * 
     * @param id The discord id of the user
     * @param firstConnection The new status
     * @throws SQLException if query execution fails
     */
    public static void setFirstConnection(String id, boolean firstConnection) throws SQLException {
        executeSQLCommand("UPDATE DiscordAccount SET firstConnection = ? WHERE id = ?", firstConnection ? 1 : 0, id);
    }
}
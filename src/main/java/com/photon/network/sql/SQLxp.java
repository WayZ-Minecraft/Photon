package com.photon.network.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.photon.network.sql.customExeption.LevelNotFoundExepction;
import com.photon.network.sql.customExeption.PlayerNotFoundException;
import com.photon.util.ConsoleManager;

/**
 * XP and level management for user progression system.
 * Uses PreparedStatement for SQL injection protection.
 * 
 * @author noz43
 * @version 1.0
 */
public class SQLxp extends SqlInteract {

    /**
     * Retrieve the top players by level and XP.
     * 
     * @param top Number of users to retrieve
     * @return List of String arrays [id, level, xp]
     * @throws SQLException if query execution fails
     */
    public static List<String[]> getLeaderboard(int top) throws SQLException {
        ArrayList<String[]> leadboard = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.prepareStatement(
                "SELECT id, xp, level FROM User ORDER BY level DESC, xp DESC LIMIT ?");
            statement.setInt(1, top);
            resultat = statement.executeQuery();

            while (resultat.next()) {
                String id = resultat.getString("id");
                String level = resultat.getString("level");
                String xp = resultat.getString("xp");
                
                String[] user = {id, level, xp};
                leadboard.add(user);
            }
        } catch (SQLException e) {
            if (reconnect()) return getLeaderboard(top);
            else {
                ConsoleManager.create("Error getting leaderboard: " + e.getMessage()).error().end();
                throw e;
            }
        } finally {
            closeStatement(statement, resultat);
        }

        return leadboard;
    }

    /**
     * Set user XP to a specific value.
     * 
     * @param id The discord id of the user
     * @param number The new XP value
     */
    public static void setXp(String id, int number){
        PreparedStatement statement = null;
        
        try {
            statement = connexion.prepareStatement("UPDATE User SET xp = ? WHERE id = ?");
            statement.setInt(1, number);
            statement.setString(2, id);
            
            int nb = statement.executeUpdate();
            if (nb == 0) throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            setXp(id, number);
        } catch (SQLException e) {
            if (reconnect()) setXp(id, number);
            else ConsoleManager.create("Error setting xp for " + id + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Add XP to a user's current total.
     * 
     * @param id The discord id of the user
     * @param number Amount of XP to add
     */
    public static void addXp(String id, int number){
        PreparedStatement statement = null;
        
        try {
            statement = connexion.prepareStatement("UPDATE User SET xp = xp + ? WHERE id = ?");
            statement.setInt(1, number);
            statement.setString(2, id);
            
            int nb = statement.executeUpdate();
            if (nb == 0) throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            addXp(id, number);
        } catch (SQLException e) {
            if (reconnect()) addXp(id, number);
            else ConsoleManager.create("Error adding xp to " + id + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Get the current level of a user.
     * 
     * @param id The discord id of the user
     * @return The user's level
     * @throws SQLException if query execution fails
     */
    public static int getLevel(String id) throws SQLException{
        PreparedStatement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.prepareStatement("SELECT level FROM User WHERE id = ?");
            statement.setString(1, id);
            resultat = statement.executeQuery();

            if (resultat.next()) return resultat.getInt("level");
            else throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            return 1;
        } catch (SQLException e) {
            if (reconnect()) return getLevel(id);
            else {
                ConsoleManager.create("Error getting level for " + id + ": " + e.getMessage()).error().end();
                throw e;
            }
        } finally {
            closeStatement(statement, resultat);
        }
    }

    /**
     * Set user level to a specific value.
     * 
     * @param id The discord id of the user
     * @param level The new level value
     */
    public static void setLevel(String id, int level){
        PreparedStatement statement = null;
        
        try {
            statement = connexion.prepareStatement("UPDATE User SET level = ? WHERE id = ?");
            statement.setInt(1, level);
            statement.setString(2, id);
            
            int nb = statement.executeUpdate();
            if (nb == 0) throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            setLevel(id, level);
        } catch (SQLException e) {
            if (reconnect()) setLevel(id, level);
            else ConsoleManager.create("Error setting level for " + id + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Add levels to a user's current level.
     * 
     * @param id The discord id of the user
     * @param level Number of levels to add
     */
    public static void addLevel(String id, int level){
        PreparedStatement statement = null;
        
        try {
            statement = connexion.prepareStatement("UPDATE User SET level = level + ? WHERE id = ?");
            statement.setInt(1, level);
            statement.setString(2, id);
            
            int nb = statement.executeUpdate();
            if (nb == 0) throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
        } catch (SQLException e) {
            if (reconnect()) addLevel(id, level);
            else ConsoleManager.create("Error adding level to " + id + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Get XP requirement for a specific level.
     * 
     * @param level The level number
     * @return Required XP amount
     * @throws SQLException if query execution fails
     */
    public static int getXpLevel(int level) throws SQLException{
        PreparedStatement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.prepareStatement("SELECT xpLevel FROM Level WHERE number = ?");
            statement.setInt(1, level);
            resultat = statement.executeQuery();

            if (resultat.next()) return resultat.getInt("xpLevel");
            else throw new LevelNotFoundExepction("Level not found");

        } catch (LevelNotFoundExepction e) {
            ConsoleManager.create("Error getting xp for level " + level + ": " + e.getMessage()).error().displayOnDiscord().end();
            return 500000;
        } catch (SQLException e) {
            if (reconnect()) return getXpLevel(level);
            else {
                ConsoleManager.create("Error getting xp for level " + level + ": " + e.getMessage()).error().end();
                throw e;
            }
        } finally {
            closeStatement(statement, resultat);
        }
    }

    /**
     * Get the current XP of a user.
     * 
     * @param id The discord id of the user
     * @return The user's XP
     * @throws SQLException if query execution fails
     */
    public static int getXp(String id) throws SQLException{
        PreparedStatement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.prepareStatement("SELECT xp FROM User WHERE id = ?");
            statement.setString(1, id);
            resultat = statement.executeQuery();

            if (resultat.next()) return resultat.getInt("xp");
            else throw new PlayerNotFoundException("User not found");

        } catch (PlayerNotFoundException e) {
            addUser(id);
            return 0;
        } catch (SQLException e) {
            if (reconnect()) return getXp(id);
            else {
                ConsoleManager.create("Error getting xp for " + id + ": " + e.getMessage()).error().end();
                throw e;
            }
        } finally {
            closeStatement(statement, resultat);
        }
    }

    /**
     * Get XP required to reach next level for a user.
     * More efficient than getXpLevel(getLevel(id)).
     * 
     * @param id The discord id of the user
     * @return XP required for next level
     * @throws LevelNotFoundExepction if level doesn't exist in Level table
     * @throws SQLException if query execution fails
     */
    public static int getXpToNextLevel(String id) throws LevelNotFoundExepction, SQLException{
        PreparedStatement statement = null;
        ResultSet resultat = null;

        try {
            statement = connexion.prepareStatement(
                "SELECT xpLevel FROM Level WHERE number = (SELECT level FROM User WHERE id = ?)");
            statement.setString(1, id);
            resultat = statement.executeQuery();

            if (resultat.next()) return resultat.getInt("xpLevel");
            else throw new LevelNotFoundExepction("Level not found");

        } catch (SQLException e) {
            if (reconnect()) return getXpToNextLevel(id);
            else {
                ConsoleManager.create("Error getting xp for user " + id + ": " + e.getMessage()).error().end();
                throw e;
            }
        } finally {
            closeStatement(statement, resultat);
        }
    }

    /**
     * Get the leaderboard rank of a user.
     * 
     * @param id The discord id of the user
     * @return The user's rank (1-based)
     * @throws SQLException if query execution fails
     */
    public static int getRank(String id) throws SQLException{
        PreparedStatement statement = null;
        ResultSet resultat = null;
        
        try {
            statement = connexion.prepareStatement(
                "SELECT COUNT(*) FROM User WHERE level > (SELECT level FROM User WHERE id = ?) " +
                "OR (level = (SELECT level FROM User WHERE id = ?) AND xp >= (SELECT xp FROM User WHERE id = ?))");
            statement.setString(1, id);
            statement.setString(2, id);
            statement.setString(3, id);
            resultat = statement.executeQuery();

            int rank = 0;
            if (resultat.next()) rank = resultat.getInt("COUNT(*)");

            if (rank == 0) throw new PlayerNotFoundException("User not found");
            return rank;

        } catch (SQLException e) {
            if (reconnect()) return getRank(id);
            else {
                ConsoleManager.create("Error getting rank for " + id + ": " + e.getMessage()).error().end();
                throw e;
            }
        } catch (PlayerNotFoundException e) {
            addUser(id);
            return getRank(id);
        } finally {
            closeStatement(statement, resultat);
        }
    }
}
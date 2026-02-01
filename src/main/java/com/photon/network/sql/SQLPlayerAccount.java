package com.photon.network.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.util.ConsoleManager;

/**
 * Player account management with SQLite database.
 * Handles account creation, retrieval, validation and deletion.
 * 
 * @author noz43
 */
public class SQLPlayerAccount extends SqlInteract {

    private static final Gson gson = new GsonBuilder().create();

    /**
     * Create a new player account.
     * Validates input and checks for existing email/username before creation.
     * 
     * @param username The desired username (must be unique)
     * @param email The email address (must be unique)
     * @param password The password (already hashed)
     * @return ObjectPlayerAccount if successful, null otherwise
     */
    public static ObjectPlayerAccount createAccount(String username, String email, String password) {
        if (username == null || email == null || password == null) {
            ConsoleManager.create("Cannot create account with null parameters").error().end();
            return null;
        }
        
        if (username.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) {
            ConsoleManager.create("Cannot create account with empty parameters").error().end();
            return null;
        }
        
        if (emailExists(email)) {
            ConsoleManager.create("Email already exists: " + email).error().end();
            return null;
        }
        
        if (usernameExists(username)) {
            ConsoleManager.create("Username already exists: " + username).error().end();
            return null;
        }

        PreparedStatement statement = null;

        try {
            String uuid = UUID.randomUUID().toString();
            String discordAuthCode = ObjectPlayerAccount.generateAuthCode();
            
            statement = connexion.prepareStatement(
                "INSERT INTO PlayerAccount (uuid, username, email, password, discordAuthCode, friends) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, uuid);
            statement.setString(2, username.trim());
            statement.setString(3, email.trim().toLowerCase());
            statement.setString(4, password);
            statement.setString(5, discordAuthCode);
            statement.setString(6, "[]");
            statement.executeUpdate();

            return getAccountByUUID(uuid);

        } catch (SQLException e) {
            if (reconnect())
                return createAccount(username, email, password);
            else {
                ConsoleManager.create("Error creating account for " + username + ": " + e.getMessage()).error().end();
                return null;
            }
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Retrieve account by UUID.
     * 
     * @param uuid The unique identifier
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectPlayerAccount getAccountByUUID(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }

        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT * FROM PlayerAccount WHERE uuid = ?");
            statement.setString(1, uuid);
            result = statement.executeQuery();

            if (result.next()) {
                return mapResultSetToAccount(result);
            }
            return null;

        } catch (SQLException e) {
            if (reconnect())
                return getAccountByUUID(uuid);
            else {
                ConsoleManager.create("Error getting account by UUID " + uuid + ": " + e.getMessage()).error().end();
                return null;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Retrieve account by email address.
     * Email comparison is case-insensitive.
     * 
     * @param email The email address
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectPlayerAccount getAccountByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT * FROM PlayerAccount WHERE LOWER(email) = LOWER(?)");
            statement.setString(1, email.trim());
            result = statement.executeQuery();

            if (result.next()) {
                return mapResultSetToAccount(result);
            }
            return null;

        } catch (SQLException e) {
            if (reconnect())
                return getAccountByEmail(email);
            else {
                ConsoleManager.create("Error getting account by email " + email + ": " + e.getMessage()).error().end();
                return null;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Retrieve account by username.
     * Username comparison is case-insensitive.
     * 
     * @param username The username
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectPlayerAccount getAccountByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT * FROM PlayerAccount WHERE LOWER(username) = LOWER(?)");
            statement.setString(1, username.trim());
            result = statement.executeQuery();

            if (result.next()) {
                return mapResultSetToAccount(result);
            }
            return null;

        } catch (SQLException e) {
            if (reconnect())
                return getAccountByUsername(username);
            else {
                ConsoleManager.create("Error getting account by username " + username + ": " + e.getMessage()).error().end();
                return null;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Retrieve account by Discord ID.
     * 
     * @param discordID The Discord user ID
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectPlayerAccount getAccountByDiscordID(String discordID) {
        if (discordID == null || discordID.trim().isEmpty()) {
            return null;
        }

        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT * FROM PlayerAccount WHERE discordID = ?");
            statement.setString(1, discordID);
            result = statement.executeQuery();

            if (result.next()) {
                return mapResultSetToAccount(result);
            }
            return null;

        } catch (SQLException e) {
            if (reconnect())
                return getAccountByDiscordID(discordID);
            else {
                ConsoleManager.create("Error getting account by Discord ID " + discordID + ": " + e.getMessage()).error().end();
                return null;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Check if an email is already registered.
     * Email comparison is case-insensitive.
     * 
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    public static boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT COUNT(*) FROM PlayerAccount WHERE LOWER(email) = LOWER(?)");
            statement.setString(1, email.trim());
            result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            if (reconnect())
                return emailExists(email);
            else {
                ConsoleManager.create("Error checking email existence: " + e.getMessage()).error().end();
                return false;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Check if a username is already taken.
     * Username comparison is case-insensitive.
     * 
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public static boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT COUNT(*) FROM PlayerAccount WHERE LOWER(username) = LOWER(?)");
            statement.setString(1, username.trim());
            result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            if (reconnect())
                return usernameExists(username);
            else {
                ConsoleManager.create("Error checking username existence: " + e.getMessage()).error().end();
                return false;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Get Discord authentication token by email.
     * 
     * @param email The email address
     * @return The auth code if found, null otherwise
     */
    public static String getTokenByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT discordAuthCode FROM PlayerAccount WHERE LOWER(email) = LOWER(?)");
            statement.setString(1, email.trim());
            result = statement.executeQuery();

            if (result.next()) {
                return result.getString("discordAuthCode");
            }
            return null;

        } catch (SQLException e) {
            if (reconnect())
                return getTokenByEmail(email);
            else {
                ConsoleManager.create("Error getting token by email: " + e.getMessage()).error().end();
                return null;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Validate authentication code for a given UUID.
     * Uses direct SQL comparison for better performance.
     * 
     * @param givenUUID The player UUID
     * @param givenAuthCode The authentication code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isAuthCodeValid(String givenUUID, String givenAuthCode) {
        if (givenUUID == null || givenAuthCode == null) {
            return false;
        }
        
        if (givenUUID.trim().isEmpty() || givenAuthCode.trim().isEmpty()) {
            return false;
        }

        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement(
                "SELECT COUNT(*) FROM PlayerAccount WHERE uuid = ? AND discordAuthCode = ?");
            statement.setString(1, givenUUID);
            statement.setString(2, givenAuthCode);
            result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            if (reconnect())
                return isAuthCodeValid(givenUUID, givenAuthCode);
            else {
                ConsoleManager.create("Error validating auth code: " + e.getMessage()).error().end();
                return false;
            }
        } finally {
            closeStatement(statement, result);
        }
    }

    /**
     * Delete an account by UUID.
     * 
     * @param uuid The account UUID to delete
     */
    public static void deleteAccount(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            ConsoleManager.create("Cannot delete account with null/empty UUID").error().end();
            return;
        }

        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement("DELETE FROM PlayerAccount WHERE uuid = ?");
            statement.setString(1, uuid);
            int affected = statement.executeUpdate();
            
            if (affected == 0) {
                ConsoleManager.create("No account found with UUID: " + uuid).error().end();
            }

        } catch (SQLException e) {
            if (reconnect())
                deleteAccount(uuid);
            else
                ConsoleManager.create("Error deleting account " + uuid + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

    /**
     * Retrieve all player accounts from database.
     * 
     * @return ArrayList of all accounts
     */
    public static ArrayList<ObjectPlayerAccount> getAllAccounts() {
        ArrayList<ObjectPlayerAccount> accounts = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT * FROM PlayerAccount");
            result = statement.executeQuery();

            while (result.next()) {
                accounts.add(mapResultSetToAccount(result));
            }

        } catch (SQLException e) {
            if (reconnect())
                return getAllAccounts();
            else {
                ConsoleManager.create("Error getting all accounts: " + e.getMessage()).error().end();
            }
        } finally {
            closeStatement(statement, result);
        }

        return accounts;
    }

    /**
     * Map ResultSet to ObjectPlayerAccount.
     * Handles JSON deserialization for friends list.
     * 
     * @param result The ResultSet from query
     * @return Populated ObjectPlayerAccount
     * @throws SQLException if data access fails
     */
    private static ObjectPlayerAccount mapResultSetToAccount(ResultSet result) throws SQLException {
        ObjectPlayerAccount account = new ObjectPlayerAccount();
        account.uuid = result.getString("uuid");
        account.username = result.getString("username");
        account.email = result.getString("email");
        account.password = result.getString("password");
        account.twoAuthFactor = result.getInt("twoAuthFactor") == 1;
        account.discordID = result.getString("discordID");
        account.discordAuthCode = result.getString("discordAuthCode");
        account.projectCreator = result.getInt("projectCreator") == 1;
        account.shopCoins = result.getInt("shopCoins");
        
        String friendsJson = result.getString("friends");
        if (friendsJson != null && !friendsJson.isEmpty()) {
            try {
                TypeToken<ArrayList<String>> typeToken = new TypeToken<ArrayList<String>>() {};
                account.firends = gson.fromJson(friendsJson, typeToken.getType());
            } catch (Exception e) {
                ConsoleManager.create("Error parsing friends JSON for " + account.uuid + ": " + e.getMessage()).error().end();
                account.firends = new ArrayList<>();
            }
        } else {
            account.firends = new ArrayList<>();
        }
        
        return account;
    }
}
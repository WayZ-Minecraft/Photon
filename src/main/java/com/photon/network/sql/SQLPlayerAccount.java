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

public class SQLPlayerAccount extends SqlInteract {

    private static final Gson gson = new GsonBuilder().create();

    public static ObjectPlayerAccount createAccount(String username, String email, String password) {
        PreparedStatement statement = null;

        try {
            String uuid = UUID.randomUUID().toString();
            String discordAuthCode = ObjectPlayerAccount.generateAuthCode();
            
            statement = connexion.prepareStatement(
                "INSERT INTO PlayerAccount (uuid, username, email, password, discordAuthCode, friends) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.setString(3, email);
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

    public static ObjectPlayerAccount getAccountByUUID(String uuid) {
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

    public static ObjectPlayerAccount getAccountByEmail(String email) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT * FROM PlayerAccount WHERE email = ?");
            statement.setString(1, email);
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

    public static ObjectPlayerAccount getAccountByUsername(String username) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT * FROM PlayerAccount WHERE username = ?");
            statement.setString(1, username);
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

    public static ObjectPlayerAccount getAccountByDiscordID(String discordID) {
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

    public static boolean emailExists(String email) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT COUNT(*) FROM PlayerAccount WHERE email = ?");
            statement.setString(1, email);
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

    public static boolean usernameExists(String username) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT COUNT(*) FROM PlayerAccount WHERE username = ?");
            statement.setString(1, username);
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

    public static String getTokenByEmail(String email) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connexion.prepareStatement("SELECT discordAuthCode FROM PlayerAccount WHERE email = ?");
            statement.setString(1, email);
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

    public static void deleteAccount(String uuid) {
        PreparedStatement statement = null;

        try {
            statement = connexion.prepareStatement("DELETE FROM PlayerAccount WHERE uuid = ?");
            statement.setString(1, uuid);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (reconnect())
                deleteAccount(uuid);
            else
                ConsoleManager.create("Error deleting account " + uuid + ": " + e.getMessage()).error().end();
        } finally {
            closeStatement(statement, null);
        }
    }

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
        if (friendsJson != null) {
            TypeToken<ArrayList<String>> typeToken = new TypeToken<ArrayList<String>>() {};
            account.firends = gson.fromJson(friendsJson, typeToken.getType());
        }
        
        return account;
    }
}
package com.photon.network.sql;

import java.util.List;
import java.util.UUID;

import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.NetworkOnly;

/**
 * Player account management with SQLite database.
 * Handles account creation, retrieval, validation and deletion.
 * 
 * @author noz43
 */
@NetworkOnly
public class SQLPlayerAccount extends SQLInteraction {

    /**
     * Create PlayerAccount table with columns: uuid, username, email, password, twoAuthFactor, discordID, discordAuthCode, projectAuthor, shopCoins, friends.
     */
    @Override
    public void register() {
        executeSQLCommand("CREATE TABLE IF NOT EXISTS PlayerAccount (uuid TEXT PRIMARY KEY NOT NULL, username TEXT UNIQUE NOT NULL, email TEXT UNIQUE NOT NULL, password TEXT NOT NULL, twoAuthFactor INTEGER DEFAULT 0, discordID TEXT, discordAuthCode TEXT, projectAuthor INTEGER DEFAULT 0, serverCreator INTEGER DEFAULT 0, shopCoins INTEGER DEFAULT 0, friends TEXT);");
    }

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
            ConsoleManager.create("Cannot create account with null parameters").withType(EnumLogType.SQL).error().end();
            return null;
        }
        
        if (username.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) {
            ConsoleManager.create("Cannot create account with empty parameters").withType(EnumLogType.SQL).error().end();
            return null;
        }
        
        if (emailExists(email)) {
            ConsoleManager.create("Email already exists: " + email).withType(EnumLogType.SQL).error().end();
            return null;
        }
        
        if (usernameExists(username)) {
            ConsoleManager.create("Username already exists: " + username).withType(EnumLogType.SQL).error().end();
            return null;
        }

        final String UniqueUserID = UUID.randomUUID().toString();
        executeSQLCommand("INSERT INTO PlayerAccount (uuid, username, email, password, discordAuthCode, friends) VALUES (?, ?, ?, ?, ?, ?)", 
            UniqueUserID,
            username.trim(),
            email.trim().toLowerCase(),
            password,
            ObjectPlayerAccount.generateAuthCode(),
            "[]"
        );

        return getAccountByUUID(UniqueUserID);
    }

    public static void updateDiscordID(String uuid, String discordID) {
        if (uuid == null || uuid.trim().isEmpty()) {
            ConsoleManager.create("Cannot update Discord ID for null/empty UUID").error().end();
            return;
        }
        executeSQLCommand("UPDATE PlayerAccount SET discordID = ? WHERE uuid = ?", discordID, uuid);
    }

    public static boolean existByUUID(String uuid) { return getAccountByUUID(uuid) != null; }

    /**
     * Retrieve account by UUID.
     * 
     * @param uuid The unique identifier
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectPlayerAccount getAccountByUUID(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            ConsoleManager.create("Cannot get account with null/empty UUID").error().end();
            return null;
        }
        return executeSQLCommand(ObjectPlayerAccount.class, "SELECT * FROM PlayerAccount WHERE uuid = ?", uuid);
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
            ConsoleManager.create("Cannot get account with null/empty email").error().end();
            return null;
        }
        return executeSQLCommand(ObjectPlayerAccount.class, "SELECT * FROM PlayerAccount WHERE LOWER(email) = LOWER(?)", email.trim());
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
            ConsoleManager.create("Cannot get account with null/empty username").error().end();
            return null;
        }
        return executeSQLCommand(ObjectPlayerAccount.class, "SELECT * FROM PlayerAccount WHERE LOWER(username) = LOWER(?)", username.trim());
    }

    /**
     * Retrieve account by Discord ID.
     * 
     * @param discordID The Discord user ID
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectPlayerAccount getAccountByDiscordID(String discordID) {
        if (discordID == null || discordID.trim().isEmpty()) {
            ConsoleManager.create("Cannot get account with null/empty Discord ID").error().end();
            return null;
        }
        return executeSQLCommand(ObjectPlayerAccount.class, "SELECT * FROM PlayerAccount WHERE discordID = ?", discordID);
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
            ConsoleManager.create("Cannot check existence of null/empty email").error().end();
            return false;
        }
        return (int)executeSQLCommandForPrimitive("SELECT COUNT(*) FROM PlayerAccount WHERE LOWER(email) = LOWER(?)", email.trim()) > 0;
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
            ConsoleManager.create("Cannot check existence of null/empty username").error().end();
            return false;
        }
        return (int)executeSQLCommandForPrimitive("SELECT COUNT(*) FROM PlayerAccount WHERE LOWER(username) = LOWER(?)", username.trim()) > 0;
    }

    /**
     * Get Discord authentication token by email.
     * 
     * @param email The email address
     * @return The auth code if found, null otherwise
     */
    public static String getTokenByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            ConsoleManager.create("Cannot get auth code for null/empty email").error().end();
            return null;
        }
        return (String)executeSQLCommandForPrimitive("SELECT discordAuthCode FROM PlayerAccount WHERE LOWER(email) = LOWER(?)", email.trim());
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
        if(givenUUID == null || givenUUID.trim().isEmpty() || givenAuthCode == null || givenAuthCode.trim().isEmpty()) {
            ConsoleManager.create("Cannot validate auth code with null/empty parameters").error().end();
            return false;
        }

        return (int)executeSQLCommandForPrimitive("SELECT COUNT(*) FROM PlayerAccount WHERE uuid = ? AND discordAuthCode = ?", givenUUID, givenAuthCode) > 0;
    }

    public static void setServerCreator(String uuid, boolean isServerCreator) {
        if (uuid == null || uuid.trim().isEmpty()) {
            ConsoleManager.create("Cannot update serverCreator with null/empty UUID").error().end();
            return;
        }
        executeSQLCommand("UPDATE PlayerAccount SET serverCreator = ? WHERE uuid = ?", isServerCreator ? 1 : 0, uuid);
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
        executeSQLCommand("DELETE FROM PlayerAccount WHERE uuid = ?", uuid);
    }

    /**
     * Retrieve all player accounts from database.
     * 
     * @return ArrayList of all accounts
     */
    public static List<ObjectPlayerAccount> getAllAccounts() {
        return executeSQLCommandList(ObjectPlayerAccount.class, "SELECT * FROM PlayerAccount");
    }
}
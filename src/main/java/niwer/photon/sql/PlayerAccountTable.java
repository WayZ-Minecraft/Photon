package niwer.photon.sql;

import java.util.List;
import java.util.UUID;

import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.util.PhotonLogTypes;

import niwer.lumen.Console;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.DeletionManager;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

/**
 * Player account management with SQLite database.
 * Handles account creation, retrieval, validation and deletion.
 * 
 * @author noz43
 */
public class PlayerAccountTable extends Table {

    public PlayerAccountTable(DataBase db) {
        super(db);

        this.addColumns(
            createColumn(db, "uuid", EnumColumnTypes.TEXT).primaryKey(),
            createColumn(db, "username", EnumColumnTypes.TEXT).unique().notNull(),
            createColumn(db, "email", EnumColumnTypes.TEXT).unique().notNull(),
            createColumn(db, "password", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "twoAuthFactor", EnumColumnTypes.BOOLEAN).defaultValue(false),
            createColumn(db, "discordID", 1024),
            createColumn(db, "discordAuthCode", 255).notNull(),
            createColumn(db, "projectAuthor", EnumColumnTypes.BOOLEAN).defaultValue(false),
            createColumn(db, "serverCreator", EnumColumnTypes.BOOLEAN).defaultValue(false),
            createColumn(db, "shopCoins", EnumColumnTypes.INT).defaultValue(0, Expression.of("shopCoins").isGreaterThanOrEqualTo(0)), // Default to 0 and non-negative constraint
            createColumn(db, "friends", EnumColumnTypes.TEXT)
        ).execute();
    }

    @Override public String name() { return "PlayerAccount"; }

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
            Console.log("Cannot create account with null parameters").type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        
        if (username.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) {
            Console.log("Cannot create account with empty parameters").type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        
        if (emailExists(email)) {
            Console.log("Email already exists: " + email).type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        
        if (usernameExists(username)) {
            Console.log("Username already exists: " + username).type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
            return null;
        }

        final String UniqueUserID = UUID.randomUUID().toString();
        InsertionManager.insert(PhotonEngine.DATA_BASE, PlayerAccountTable.class, "uuid", "username", "email", "password", "discordAuthCode", "friends")
            .row(UniqueUserID, username.trim(), email.trim().toLowerCase(), password, ObjectPlayerAccount.generateAuthCode(), "[]")
            .execute();

        return getAccountByUUID(UniqueUserID);
    }

    public static void updateDiscordID(String uuid, String discordID) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot update Discord ID for null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        UpdateManager.update(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .set("discordID", discordID)
            .where(Expression.of("uuid").isEqualTo(uuid))
            .execute();
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
            Console.log("Cannot get account with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("uuid").isEqualTo(uuid))
            .executeSerializable(ObjectPlayerAccount.class);
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
            Console.log("Cannot get account with null/empty email").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        final String NORMALIZED_EMAIL = email.trim().toLowerCase();
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("LOWER(email)").isEqualTo(NORMALIZED_EMAIL))
            .executeSerializable(ObjectPlayerAccount.class);
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
            Console.log("Cannot get account with null/empty username").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        final String NORMALIZED_USERNAME = username.trim().toLowerCase();
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("LOWER(username)").isEqualTo(NORMALIZED_USERNAME))
            .executeSerializable(ObjectPlayerAccount.class);
    }

    /**
     * Retrieve account by Discord ID.
     * 
     * @param discordID The Discord user ID
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectPlayerAccount getAccountByDiscordID(String discordID) {
        if (discordID == null || discordID.trim().isEmpty()) {
            Console.log("Cannot get account with null/empty Discord ID").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("discordID").isEqualTo(discordID))
            .executeSerializable(ObjectPlayerAccount.class);
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
            Console.log("Cannot check existence of null/empty email").error().container(PhotonEngine.LOGGER).send();
            return false;
        }
        final String NORMALIZED_EMAIL = email.trim().toLowerCase();
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class, "COUNT(*) as count")
            .where(Expression.of("LOWER(email)").isEqualTo(NORMALIZED_EMAIL))
            .executeHasResult();
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
            Console.log("Cannot check existence of null/empty username").error().container(PhotonEngine.LOGGER).send();
            return false;
        }
        final String NORMALIZED_USERNAME = username.trim().toLowerCase();
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class, "COUNT(*) as count")
            .where(Expression.of("LOWER(username)").isEqualTo(NORMALIZED_USERNAME))
            .executeHasResult();
    }

    /**
     * Get Discord authentication token by email.
     * 
     * @param email The email address
     * @return The auth code if found, null otherwise
     */
    public static String getTokenByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            Console.log("Cannot get auth code for null/empty email").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        final String NORMALIZED_EMAIL = email.trim().toLowerCase();
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class, "discordAuthCode")
            .where(Expression.of("LOWER(email)").isEqualTo(NORMALIZED_EMAIL))
            .executePrimitive(String.class);
    }

    /**
     * Validate authentication code for a given UUID.
     * 
     * @param givenUUID The player UUID
     * @param givenAuthCode The authentication code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isAuthCodeValid(String givenUUID, String givenAuthCode) {
        if(givenUUID == null || givenUUID.trim().isEmpty() || givenAuthCode == null || givenAuthCode.trim().isEmpty()) {
            Console.log("Cannot validate auth code with null/empty parameters").error().container(PhotonEngine.LOGGER).send();
            return false;
        }
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class, "COUNT(*) as count")
            .where(
                Expression.of("uuid").isEqualTo(givenUUID)
                    .and(Expression.of("discordAuthCode").isEqualTo(givenAuthCode))
            )
            .executeHasResult();
    }

    public static void setServerCreator(String uuid, boolean isServerCreator) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot update serverCreator with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        UpdateManager.update(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .set("serverCreator", isServerCreator)
            .where(Expression.of("uuid").isEqualTo(uuid))
            .execute();
    }

        /**
     * Delete an account by UUID.
     * 
     * @param uuid The account UUID to delete
     */
    public static void deleteAccount(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot delete account with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        DeletionManager.delete(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("uuid").isEqualTo(uuid))
            .execute();
    }

    /**
     * Retrieve all player accounts from database.
     * 
     * @return ArrayList of all accounts
     */
    public static List<ObjectPlayerAccount> getAllAccounts() {
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class).executeList(ObjectPlayerAccount.class);
    }
}
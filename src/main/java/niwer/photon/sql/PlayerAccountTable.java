package niwer.photon.sql;

import java.util.List;
import java.util.UUID;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.util.HashUtils;
import niwer.photon.util.PhotonLogTypes;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.DeletionManager;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
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
        this.addColumnsFromClass(ObjectUserAccount.class).execute();
    }

    @Override public String name() { return "Account"; }

    /**
     * Create a new player account.
     * Validates input and checks for existing email/username before creation.
     * 
     * @param username The desired username (must be unique)
     * @param email The email address (must be unique)
     * @param password The password (already hashed)
     * @return ObjectPlayerAccount if successful, null otherwise
     */
    public static ObjectUserAccount createAccount(String username, String email, String password) {
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
            final String hashedPassword = hashPassword(password);
            if (hashedPassword == null) {
                Console.log("Cannot hash password for account creation").type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
                return null;
            }

            InsertionManager.insert(PhotonEngine.DATA_BASE, PlayerAccountTable.class, "uuid", "username", "email", "password", "discordAuthCode")
                .row(UniqueUserID, username.trim(), email.trim().toLowerCase(), hashedPassword, ObjectUserAccount.generateAuthCode())
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
    public static ObjectUserAccount getAccountByUUID(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot get account with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("uuid").isEqualTo(uuid))
            .executeSerializable(ObjectUserAccount.class);
    }

    /**
     * Retrieve account by email address.
     * Email comparison is case-insensitive.
     * 
     * @param email The email address
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectUserAccount getAccountByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            Console.log("Cannot get account with null/empty email").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        final String NORMALIZED_EMAIL = email.trim().toLowerCase();
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("LOWER(email)").isEqualTo(NORMALIZED_EMAIL))
            .executeSerializable(ObjectUserAccount.class);
    }

    /**
     * Retrieve account by username.
     * Username comparison is case-insensitive.
     * 
     * @param username The username
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectUserAccount getAccountByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            Console.log("Cannot get account with null/empty username").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        final String NORMALIZED_USERNAME = username.trim().toLowerCase();
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("LOWER(username)").isEqualTo(NORMALIZED_USERNAME))
            .executeSerializable(ObjectUserAccount.class);
    }

    /**
     * Retrieve account by Discord ID.
     * 
     * @param discordID The Discord user ID
     * @return ObjectPlayerAccount if found, null otherwise
     */
    public static ObjectUserAccount getAccountByDiscordID(String discordID) {
        if (discordID == null || discordID.trim().isEmpty()) {
            Console.log("Cannot get account with null/empty Discord ID").error().container(PhotonEngine.LOGGER).send();
            return null;
        }
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .where(Expression.of("discordID").isEqualTo(discordID))
            .executeSerializable(ObjectUserAccount.class);
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
        final Integer count = SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class, "COUNT(*) as count")
            .where(Expression.of("LOWER(email)").isEqualTo(NORMALIZED_EMAIL))
            .executePrimitive(Integer.class);
        return count != null && count > 0;
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
        final Integer count = SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class, "COUNT(*) as count")
            .where(Expression.of("LOWER(username)").isEqualTo(NORMALIZED_USERNAME))
            .executePrimitive(Integer.class);
        return count != null && count > 0;
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

    public static void setAdministrator(String uuid, boolean isAdministrator) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot update administrator with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        UpdateManager.update(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .set("administrator", isAdministrator)
            .where(Expression.of("uuid").isEqualTo(uuid))
            .execute();
    }

    public static void setUsername(String uuid, String username) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot update username with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        if (username == null || username.isBlank()) {
            Console.log("Cannot update username with null/empty value").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        UpdateManager.update(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .set("username", username.trim())
            .where(Expression.of("uuid").isEqualTo(uuid))
            .execute();
    }

    public static void setEmail(String uuid, String email) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot update email with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        if (email == null || email.isBlank()) {
            Console.log("Cannot update email with null/empty value").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        UpdateManager.update(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
            .set("email", email.trim().toLowerCase())
            .where(Expression.of("uuid").isEqualTo(uuid))
            .execute();
    }

    public static void setPassword(String uuid, String password) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot update password with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return;
        }
        if (password == null || password.isBlank()) {
            Console.log("Cannot update password with null/empty value").error().container(PhotonEngine.LOGGER).send();
            return;
        }
            final String hashedPassword = hashPassword(password);
            if (hashedPassword == null) {
                Console.log("Cannot hash password for update").type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
                return;
            }

            UpdateManager.update(PhotonEngine.DATA_BASE, PlayerAccountTable.class)
                .set("password", hashedPassword)
            .where(Expression.of("uuid").isEqualTo(uuid))
            .execute();
    }

        public static boolean passwordMatches(String storedPassword, String rawPassword) {
            return HashUtils.passwordMatches(storedPassword, rawPassword);
        }

        public static boolean isArgon2Password(String password) {
            return HashUtils.isArgon2Hash(password);
        }

        public static String hashPassword(String password) {
            return HashUtils.hashPassword(password);
        }

    public static boolean isAdministrator(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            Console.log("Cannot read administrator flag with null/empty UUID").error().container(PhotonEngine.LOGGER).send();
            return false;
        }

        final ObjectUserAccount account = getAccountByUUID(uuid);
        return account != null && account.isAdministrator();
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
    public static List<ObjectUserAccount> getAllAccounts() {
        return SelectionManager.select(PhotonEngine.DATA_BASE, PlayerAccountTable.class).executeList(ObjectUserAccount.class);
    }
}
package niwer.photon.sql;

import niwer.photon.PhotonEngine;
import niwer.photon.util.TranslationManager.Language;

import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class DiscordProfileTable extends Table {
    
    public DiscordProfileTable(DataBase db) {
        super(db);

        this.addColumns(
            createColumn(db, "discord_user_id", EnumColumnTypes.TEXT).primaryKey(),
            createColumn(db, "xp", EnumColumnTypes.INT).defaultValue(0, Expression.of("xp").isGreaterThanOrEqualTo(0)), // XP should never be negative
            createColumn(db, "level", EnumColumnTypes.INT).defaultValue(1, Expression.of("level").isGreaterThanOrEqualTo(1)),
            createColumn(db, "language", Language.class).notNull().defaultValue(Language.ENGLISH),
            createColumn(db, "first_connection", EnumColumnTypes.BOOLEAN).defaultValue(true) // true by default, set to false after first connection
        ).execute();
    }

    @Override public String name() { return "DiscordAccount"; }

    /**
     * Create a profile for a user if it doesn't exist.
     * 
     * @param discordUserID The discord id of the user
     */
    public static void createProfile(String discordUserID) {
        InsertionManager.insertOrIgnore(PhotonEngine.DATA_BASE, DiscordProfileTable.class, "discord_user_id")
            .row(discordUserID)
            .execute();
    }

    /**
     * Retrieve language preferences for a user.
     * 
     * @param discordUserID The discord id of the user
     * @return List of Languages or null if user has no preferences
     */
    public static Language getLanguage(String discordUserID) {
        final var QUERY = SelectionManager.select(PhotonEngine.DATA_BASE, DiscordProfileTable.class, "language")
            .where(Expression.of("discord_user_id").isEqualTo(discordUserID));
        
        if(!QUERY.executeHasResult()) return null; // No preferences found for the user

        final String USERR_LANG = QUERY.executePrimitive(String.class);
        return Language.fromNameString(USERR_LANG);
    }

    /**
     * Update language preferences for a user.
     * 
     * @param discordUserID The discord id of the user
     * @param newUserLanguage List of Languages to set
     */
    public static void setLanguage(String discordUserID, Language newUserLanguage) {
        createProfile(discordUserID);
        UpdateManager.update(PhotonEngine.DATA_BASE, DiscordProfileTable.class)
            .set("language", newUserLanguage.name())
            .where(Expression.of("discord_user_id").isEqualTo(discordUserID))
            .execute();
    }

    /**
     * Check if this is the user's first connection.
     * 
     * @param discordUserID The discord id of the user
     * @return true if first connection, false otherwise
     */
    public static boolean isFirstConnection(String discordUserID) {
        return SelectionManager.select(PhotonEngine.DATA_BASE, DiscordProfileTable.class, "first_connection")
            .where(Expression.of("discord_user_id").isEqualTo(discordUserID))
            .executeHasResult();
    }

    /**
     * Update first connection status for a user.
     * 
     * @param discordUserID The discord id of the user
     * @param firstConnection The new status
     */
    public static void setFirstConnection(String discordUserID, boolean firstConnection) {
        createProfile(discordUserID);
        UpdateManager.update(PhotonEngine.DATA_BASE, DiscordProfileTable.class)
            .set("first_connection", firstConnection)
            .where(Expression.of("discord_user_id").isEqualTo(discordUserID))
            .execute();
    }
}
package com.photon.sql;

import java.sql.SQLException;

import com.photon.network.NetworkEngine;
import com.photon.util.NetworkOnly;
import com.photon.util.TranslationManager.Language;

import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

@NetworkOnly
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
     * Retrieve language preferences for a user.
     * 
     * @param discordUserID The discord id of the user
     * @return List of Languages or null if user has no preferences
     * @throws SQLException if query execution fails
     */
    public static Language getLanguages(String discordUserID) {
        final String USER_LANG = SelectionManager.select(NetworkEngine.DATA_BASE, DiscordProfileTable.class, "language")
            .where(Expression.of("discord_user_id").isEqualTo(discordUserID))
            .executePrimitive(String.class);

        return Language.fromString(USER_LANG);
    }

    /**
     * Update language preferences for a user.
     * 
     * @param discordUserID The discord id of the user
     * @param newUSerLanguage List of Languages to set
     * @throws SQLException if query execution fails
     */
    public static void setLanguages(String discordUserID, Language newUSerLanguage) {
        UpdateManager.update(NetworkEngine.DATA_BASE, DiscordProfileTable.class)
            .set("language", newUSerLanguage)
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
        return SelectionManager.select(NetworkEngine.DATA_BASE, DiscordProfileTable.class, "first_connection")
            .where(Expression.of("discord_user_id").isEqualTo(discordUserID))
            .executeHasResult();
    }

    /**
     * Update first connection status for a user.
     * 
     * @param discordUserID The discord id of the user
     * @param firstConnection The new status
     * @throws SQLException if query execution fails
     */
    public static void setFirstConnection(String discordUserID, boolean firstConnection) throws SQLException {
        UpdateManager.update(NetworkEngine.DATA_BASE, DiscordProfileTable.class)
            .set("first_connection", firstConnection)
            .where(Expression.of("discord_user_id").isEqualTo(discordUserID))
            .execute();
    }
}
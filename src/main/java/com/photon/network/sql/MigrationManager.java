package com.photon.network.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.photon.PhotonEngine;
import com.photon.util.PhotonLogTypes;

import niwer.lumen.Console;

/**
 * @author Niwer
 * //TODO make this more Generic and use it for future updates (like adding new tables, etc)
 */
public class MigrationManager {

    public static void migrate() {
        /* Rename ModerationLog table if it exists */
        try {
            final PreparedStatement st = SQLInteraction.getConnection().prepareStatement("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='ModerationLog'");
            final ResultSet rs = st.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                SQLInteraction.executeSQLCommand("ALTER TABLE ModerationLog RENAME TO DiscordLog");
            SQLInteraction.closeStatement(st, rs);
        } catch (SQLException e) {
            Console.log("Migration rename table error: " + e.getMessage()).type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
        }
        migrateColumn("PlayerAccount", "projectAuthor", "INTEGER DEFAULT 0");
        migrateColumn("PlayerAccount", "serverCreator", "INTEGER DEFAULT 0");
        migrateColumn("PlayerAccount", "shopCoins", "INTEGER DEFAULT 0");
        migrateColumn("PlayerAccount", "friends", "TEXT");
        migrateColumn("PlayerAccount", "discordID", "TEXT");
        migrateColumn("PlayerAccount", "discordAuthCode", "TEXT");
        migrateColumn("PlayerAccount", "twoAuthFactor", "INTEGER DEFAULT 0");
    }

    private static void migrateColumn(String table, String column, String definition) {
        try {
            final PreparedStatement statement = SQLInteraction.getConnection().prepareStatement("SELECT COUNT(*) FROM pragma_table_info(?) WHERE name = ?");
            statement.setString(1, table);
            statement.setString(2, column);
            final ResultSet result = statement.executeQuery();
            final boolean exists = result.next() && result.getInt(1) > 0;
            SQLInteraction.closeStatement(statement, result);
            if (!exists) SQLInteraction.executeSQLCommand("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (SQLException e) {
            Console.log("Migration error for " + table + "." + column + ": " + e.getMessage()).type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
        }
    }
}

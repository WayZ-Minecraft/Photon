package com.photon.network.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.photon.util.NetworkOnly;

@NetworkOnly
public class SQLDiscordLog extends SQLInteraction implements SQLInteraction.SQLCommandSerializer<SQLDiscordLog> {

    public enum ModerationType {
        BAN,
        UNBAN,
        KICK,
        TIMEOUT
    }

    public int id;
    public String guildID;
    public String discordID;
    public String playerUUID;
    public String type;
    public String reason;
    public String moderatorDiscordID;
    public long durationSeconds;
    public String timestamp;

    /**
     * Create DiscordLog table with columns: id, guildID, discordID, playerUUID, type, reason, moderatorDiscordID, durationSeconds, timestamp.
     * playerUUID is nullable — only set if the Discord account is linked to a PlayerAccount.
     * durationSeconds is only relevant for TIMEOUT events (0 otherwise).
     */
    @Override
    public void register() {
        executeSQLCommand(
            "CREATE TABLE IF NOT EXISTS DiscordLog (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "guildID TEXT NOT NULL, " +
            "discordID TEXT NOT NULL, " +
            "playerUUID TEXT, " +
            "type TEXT NOT NULL, " +
            "reason TEXT, " +
            "moderatorDiscordID TEXT, " +
            "durationSeconds INTEGER DEFAULT 0, " +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);"
        );
    }

    /**
     * @param guildID            The Discord guild ID
     * @param discordID          The target user's Discord ID
     * @param type               The type of moderation action
     * @param reason             The reason for the action (nullable)
     * @param moderatorDiscordID The moderator's Discord ID (nullable)
     * @param durationSeconds    Duration in seconds for TIMEOUT, 0 otherwise
     */
    public static void save(String guildID, String discordID, ModerationType type, String reason, String moderatorDiscordID, long durationSeconds) {
        final String playerUUID = resolvePlayerUUID(discordID);
        executeSQLCommand(
            "INSERT INTO DiscordLog (guildID, discordID, playerUUID, type, reason, moderatorDiscordID, durationSeconds) VALUES (?, ?, ?, ?, ?, ?, ?)",
            guildID, discordID, playerUUID, type.name(), reason, moderatorDiscordID, durationSeconds
        );
    }

    public static void save(String guildID, String discordID, ModerationType type, String reason, String moderatorDiscordID) {
        save(guildID, discordID, type, reason, moderatorDiscordID, 0L);
    }

    public static List<SQLDiscordLog> getByDiscordID(String discordID) {
        return executeSQLCommandList(SQLDiscordLog.class, "SELECT * FROM DiscordLog WHERE discordID = ? ORDER BY timestamp DESC", discordID);
    }

    public static List<SQLDiscordLog> getByPlayerUUID(String playerUUID) {
        return executeSQLCommandList(SQLDiscordLog.class, "SELECT * FROM DiscordLog WHERE playerUUID = ? ORDER BY timestamp DESC", playerUUID);
    }

    public static List<SQLDiscordLog> getByGuild(String guildID) {
        return executeSQLCommandList(SQLDiscordLog.class, "SELECT * FROM DiscordLog WHERE guildID = ? ORDER BY timestamp DESC", guildID);
    }

    public static List<SQLDiscordLog> getByGuildAndType(String guildID, ModerationType type) {
        return executeSQLCommandList(SQLDiscordLog.class, "SELECT * FROM DiscordLog WHERE guildID = ? AND type = ? ORDER BY timestamp DESC", guildID, type.name());
    }

    public static void deleteByDiscordID(String discordID) {
        executeSQLCommand("DELETE FROM DiscordLog WHERE discordID = ?", discordID);
    }

    private static String resolvePlayerUUID(String discordID) {
        if (discordID == null || discordID.isBlank()) return null;
        final var account = SQLPlayerAccount.getAccountByDiscordID(discordID);
        return account != null ? account.uuid : null;
    }

    @Override
    public SQLDiscordLog objectify(ResultSet resultSet) throws SQLException {
        final SQLDiscordLog log = new SQLDiscordLog();
        log.id = resultSet.getInt("id");
        log.guildID = resultSet.getString("guildID");
        log.discordID = resultSet.getString("discordID");
        log.playerUUID = resultSet.getString("playerUUID");
        log.type = resultSet.getString("type");
        log.reason = resultSet.getString("reason");
        log.moderatorDiscordID = resultSet.getString("moderatorDiscordID");
        log.durationSeconds = resultSet.getLong("durationSeconds");
        log.timestamp = resultSet.getString("timestamp");
        return log;
    }
}
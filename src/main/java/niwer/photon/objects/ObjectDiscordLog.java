package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;
import niwer.queryon.tables.api.IDefaultValue;

/**
 * @author Niwer 
 */
public class ObjectDiscordLog extends SQLSerializable<ObjectDiscordLog> {

    @IColumnField(name = "id", primaryKey = true, autoIncrement = true)
    private int id;

    @IColumnField(name = "guild_id", notNull = true)
    private String guildID;

    @IColumnField(name = "discord_user_id", notNull = true)
    private String discordID;

    @IColumnField(name = "moderation_type")
    private String moderationType;

    @IColumnField(name = "reason")
    private String reason;

    @IColumnField(name = "moderator_discord_id") // Nullable
    private String moderatorDiscordID;

    @IColumnField(name = "duration_seconds", defaultValue = @IDefaultValue(value = "0"))
    private long durationSeconds = 0; // Only relevant for TIMEOUT

    @IColumnField(name = "timestamp", defaultValue = @IDefaultValue(value = "CURRENT_TIMESTAMP"))
    private Date timestamp = new Date();
}

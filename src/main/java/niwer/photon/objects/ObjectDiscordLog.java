package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectDiscordLog extends SQLSerializable<ObjectDiscordLog> {

    @IColumnField(name = "id", primaryKey = true, autoIncrement = true)
    private int userId;

    @IColumnField(name = "guild_id", notNull = true)
    private String guildID;

    @IColumnField(name = "discord_user_id", notNull = true)
    private String discordID;

    @IColumnField(name = "type", notNull = true)
    private String type;

    @IColumnField(name = "reason")
    private String reason;

    @IColumnField(name = "moderator_discord_id")
    private String moderatorDiscordID;

    @IColumnField(name = "duration_seconds")
    private long durationSeconds = 0;

    @IColumnField(name = "timestamp")
    private Date timestamp = new Date();
}

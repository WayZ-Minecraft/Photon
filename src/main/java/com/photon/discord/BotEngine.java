package com.photon.discord;

import java.awt.Color;
import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.slf4j.LoggerFactory;

import com.photon.discord.commands.CommandsManager;
import com.photon.discord.language.Languages;
import com.photon.discord.language.UsersInfo;
import com.photon.network.NetworkDirectories;
import com.photon.network.sql.SQLDiscordLog;
import com.photon.network.sql.SQLDiscordLog.ModerationType;
import com.photon.network.sql.SQLDiscordProfile;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.TranslationManager;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

/**
 * Main class of the bot, load the bot and register important slash commands
 */
public class BotEngine extends ListenerAdapter {

    private static JDABuilder botBuilder;
    private static boolean isRestarting = false;

    public static Guild guild;

    /**
     * Load the bot, register slash commands and start it
     * 
     * @throws LoginException
     * @throws InterruptedException
     */
    public static void load(boolean shouldRestart) throws LoginException, InterruptedException {
        final Logger JDA_LOGGER = (Logger) LoggerFactory.getLogger("net.dv8tion.jda");
        JDA_LOGGER.setLevel(ch.qos.logback.classic.Level.WARN);
        
        botBuilder = JDABuilder.createDefault(NetworkDirectories.getConfig().discord_bot_token);
        botBuilder.setActivity(Activity.playing(NetworkDirectories.getConfig().bot_activity));
        botBuilder.addEventListeners(new BotEngine());
        botBuilder.addEventListeners(new CommandsManager());
        botBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        botBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        botBuilder.enableIntents(GatewayIntent.GUILD_PRESENCES);
        botBuilder.enableIntents(GatewayIntent.GUILD_MODERATION);
        botBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        botBuilder.build();

        isRestarting = shouldRestart;

        CommandsManager.load();
        TranslationManager.loadAllLanguages("lang");

        ConsoleManager.create("Discord Bot connection established").withType(EnumLogType.NETWORK).end();

        Thread.sleep(550);
    }

    /**
     * When the bot is ready, register global slash commands
     */
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        event.getJDA().updateCommands().addCommands(CommandsManager.getGlobalCommands()).queue();
    }

    /**
     * Register slash commands when the bot is ready
     * 
     * @param event The event of the bot being ready
     * @author Mini
     */
    @Override
    public void onGuildReady(@Nonnull GuildReadyEvent event) {
        guild = event.getGuild();
        guild.updateCommands().addCommands(CommandsManager.getGuildCommands()).queue();
        if (isRestarting) {
            guild.getTextChannelById(NetworkDirectories.getConfig().network_console_channel_id).sendMessage("Network restarted").queue();
            isRestarting = false;
        }
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        final String guildID = event.getGuild().getId();
        final String targetID = event.getUser().getId();

        event.getGuild().retrieveAuditLogs().type(ActionType.BAN).limit(1).queue(entries -> {
            String moderatorID = null;
            String reason = null;

            if (!entries.isEmpty()) {
                final AuditLogEntry entry = entries.get(0);
                if (entry.getTargetId().equals(targetID)) {
                    moderatorID = entry.getUser() != null ? entry.getUser().getId() : null;
                    reason = entry.getReason();
                }
            }

            SQLDiscordLog.save(guildID, targetID, ModerationType.BAN, reason, moderatorID);
        });
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        final String guildID = event.getGuild().getId();
        final String targetID = event.getUser().getId();

        event.getGuild().retrieveAuditLogs().type(ActionType.UNBAN).limit(1).queue(entries -> {
            String moderatorID = null;

            if (!entries.isEmpty()) {
                final AuditLogEntry entry = entries.get(0);
                if (entry.getTargetId().equals(targetID))
                    moderatorID = entry.getUser() != null ? entry.getUser().getId() : null;
            }

            SQLDiscordLog.save(guildID, targetID, ModerationType.UNBAN, null, moderatorID);
        });
    }

    /**
     * When a user leave the server, remove him from the database
     * 
     * @param event The event of a user leaving the server
     */
    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        SQLDiscordProfile.setLanguages(event.getUser().getId(), new ArrayList<>());

        final String guildID = event.getGuild().getId();
        final String targetID = event.getUser().getId();

        /* Check audit log to detect kick vs voluntary leave */
        event.getGuild().retrieveAuditLogs().type(ActionType.KICK).limit(1).queue(entries -> {
            if (entries.isEmpty()) return;

            final AuditLogEntry entry = entries.get(0);
            final long secondsSinceAction = Duration.between(entry.getTimeCreated(), OffsetDateTime.now()).abs().getSeconds();
            if (!entry.getTargetId().equals(targetID) || secondsSinceAction > 3) return;

            SQLDiscordLog.save(guildID, targetID, ModerationType.KICK, entry.getReason(), entry.getUser() != null ? entry.getUser().getId() : null);
        });
    }

    @Override
    public void onGuildMemberUpdateTimeOut(@Nonnull GuildMemberUpdateTimeOutEvent event) {
        final OffsetDateTime timeoutEnd = event.getNewTimeOutEnd();
        if (timeoutEnd == null) return;

        final String guildID = event.getGuild().getId();
        final String targetID = event.getUser().getId();
        final long durationSeconds = Duration.between(OffsetDateTime.now(), timeoutEnd).getSeconds();

        event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(1).queue(entries -> {
            String moderatorID = null;
            String reason = null;

            if (!entries.isEmpty()) {
                final AuditLogEntry entry = entries.get(0);
                if (entry.getTargetId().equals(targetID)) {
                    moderatorID = entry.getUser() != null ? entry.getUser().getId() : null;
                    reason = entry.getReason();
                }
            }

            SQLDiscordLog.save(guildID, targetID, ModerationType.TIMEOUT, reason, moderatorID, durationSeconds);
        });
    }

    /**
     * To log something on discord console manager channel
     * 
     * @param color   The color of the log
     * @param title   The title of the log
     * @param content The content of the log
     * @param file    The file to send with the log (if null, no file is sent)
     */
    public static void log(Color color, String title, Object content, File file) {
        if(!hasConsoleChannel() && !isOfficialGuild(guild)) return;

        final EmbedBuilder LOGS_EMBED = new EmbedBuilder();
        LOGS_EMBED.setColor(color);
        LOGS_EMBED.setTitle(title);
        LOGS_EMBED.setDescription(content.toString());
        getConsoleChannel().sendMessageEmbeds(LOGS_EMBED.build()).queue();

        if (file != null) {
            final FileUpload FILE_TO_UPLOAD = FileUpload.fromData(file, "logs.txt");
            getConsoleChannel().sendFiles(FILE_TO_UPLOAD).queue();
        }
    }

    public static boolean isOfficialGuild(Guild guildToCheck) {
        if(guildToCheck == null) return false;
        return guildToCheck.getId().equals(NetworkDirectories.getConfig().official_discord_server_id);
    }

    public static boolean hasConsoleChannel() { return getConsoleChannel() != null; }

    public static boolean isConsoleChannel(GuildChannel channel) {
        if(channel == null) return false;
        if(!hasConsoleChannel()) return false;
        return channel.getId().equals(getConsoleChannel().getId());
    }

    /**
     * @return The console channel where the bot/network logs arrive
     */
    public static StandardGuildMessageChannel getConsoleChannel() {
        if(guild == null) return null;
        if(NetworkDirectories.getConfig().network_console_channel_id == null || NetworkDirectories.getConfig().network_console_channel_id.isEmpty()) return null;
        return guild.getTextChannelById(NetworkDirectories.getConfig().network_console_channel_id);
    }

    /**
     * Handle slash commands
     * 
     * @param event The event that triggered this command
     * @author Mini
     */
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) { CommandsManager.onSlashCommand(event); }

    /**
     * When a user get a role
     * 
     * @param event The event of a user getting a role
     * @author Mini
     */
    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        for (Role role : event.getRoles()){
            if (role.getIdLong() == Roles.FR.id) UsersInfo.addLanguages(event.getUser().getId(), Languages.FRENCH);
            else if (role.getIdLong() == Roles.EN.id) UsersInfo.addLanguages(event.getUser().getId(), Languages.ENGLISH);
        }
    }

    /**
     * When a user lose a role
     * 
     * @param event The event of a user losing a role
     */
    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        for (Role role : event.getRoles()){
            if (role.getIdLong() == Roles.FR.id) UsersInfo.removeLanguages(event.getUser().getId(), Languages.FRENCH);
            else if (role.getIdLong() == Roles.EN.id) UsersInfo.removeLanguages(event.getUser().getId(), Languages.ENGLISH);
        }
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        handleMessages(event.getGuildChannel(), event.getMessage());
    }
    
    /**
     * When a message is updated
     * 
     * @param event The event of a message being update
     */
    @Override
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {
        handleMessages(event.getGuildChannel(), event.getMessage());
    }

    // TODO Add listen to ban/kick/other moderation events ! -> Save all that stuff in the database and link it to the discord user profile !

    private static void handleMessages(GuildChannel eventChannel, Message message) {
        final User AUTHOR = message.getAuthor();
        if (AUTHOR.isBot()) return;

        if(isConsoleChannel(eventChannel)) {
            message.delete().queue();
            return;
        }

        final boolean IS_LINK = DiscordSecurity.checkLink(message.getContentRaw());
        if (IS_LINK) {
            message.delete().queue();
            AUTHOR.openPrivateChannel().queue(pm -> pm.sendMessage(TranslationManager.format(UsersInfo.getLanguage(AUTHOR.getId()).code,"discord.securityMessage.updateMessage")).queue());
        }
    }

    public static boolean isBotInitialized() { return botBuilder != null; }
}
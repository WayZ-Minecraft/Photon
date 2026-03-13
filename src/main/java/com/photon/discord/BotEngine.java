package com.photon.discord;

import java.time.Duration;
import java.time.OffsetDateTime;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.slf4j.LoggerFactory;

import com.photon.PhotonEngine;
import com.photon.discord.commands.CommandsManager;
import com.photon.discord.language.UsersInfo;
import com.photon.network.NetworkDirectories;
import com.photon.network.sql.SQLDiscordLog;
import com.photon.network.sql.SQLDiscordLog.ModerationType;
import com.photon.util.PhotonLogTypes;
import com.photon.util.TranslationManager;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import niwer.lumen.Console;

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

        Console.log("Discord Bot connection established").type(PhotonLogTypes.DISCORD_BOT).container(PhotonEngine.LOGGER).send();

        Thread.sleep(550); // Wait a bit to ensure guild is loaded
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

    /**
     * Log a moderation action (ban, unban, kick, timeout) in the database without duration (for bans, unbans and kicks)
     * @param guild The guild where the action took place
     * @param targetId The ID of the user targeted by the action
     * @param type The type of the moderation action
     */
    private static void logModerationAction(@Nonnull Guild guild, String targetId, ModerationType type) {
        logModerationAction(guild, targetId, type, 0L);
    }

    /**
     * Log a moderation action (ban, unban, kick, timeout) in the database
     * @param guild The guild where the action took place
     * @param targetId The ID of the user targeted by the action
     * @param type The type of the moderation action
     * @param durationSeconds The duration of the action in seconds (for timeouts, 0 for other actions)
     */
    private static void logModerationAction(@Nonnull Guild guild, String targetId, ModerationType type, long durationSeconds) {
        guild.retrieveAuditLogs().type(type.toDiscordActionType()).limit(1).queue(entries -> {
            if(entries.isEmpty()) return; // No entry, No log

            final AuditLogEntry AUDIT = entries.stream().filter(entry -> entry.getTargetId().equals(targetId)).findFirst().orElse(null);
            if (AUDIT == null) return; // No relevant entry, No log
            
            final String MODERATOR_ID = AUDIT.getUser() != null ? AUDIT.getUser().getId() : null;
            final String REASON = AUDIT.getReason();
            SQLDiscordLog.save(guild.getId(), targetId, type, REASON, MODERATOR_ID, durationSeconds);
        });
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        final String TARGET_USER_ID = event.getUser().getId();
        logModerationAction(event.getGuild(), TARGET_USER_ID, ModerationType.BAN);
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        final String TARGET_USER_ID = event.getUser().getId();
        logModerationAction(event.getGuild(), TARGET_USER_ID, ModerationType.UNBAN); // No reasons
    }

    @Override
    public void onGuildMemberUpdateTimeOut(@Nonnull GuildMemberUpdateTimeOutEvent event) {
        final OffsetDateTime TIMEOUT_END = event.getNewTimeOutEnd();
        if (TIMEOUT_END == null) return; // If the timeout end is null, it means the user has been removed from timeout, we log it as an untimeout with duration 0

        final long DURATION_SECONDS = Duration.between(OffsetDateTime.now(), TIMEOUT_END).getSeconds();
        final String TARGET_USER_ID = event.getUser().getId();
        logModerationAction(event.getGuild(), TARGET_USER_ID, ModerationType.TIMEOUT, DURATION_SECONDS);
    }
    
    /**
     * To log something on discord console manager channel
     * 
     * @param data The data to log, if the console channel is configured and the bot is in the official guild, it will be sent to the console channel, otherwise it will be sent to the network logs channel
     */
    public static void log(Console data) {
        if(!hasConsoleChannel() && !isOfficialGuild(guild)) return;

        final EmbedBuilder LOGS_EMBED = new EmbedBuilder();
        LOGS_EMBED.setColor(data.type().color().color());
        LOGS_EMBED.setTitle((data.isError() ? "[Error] " : "") + data.type().name());
        LOGS_EMBED.setDescription(String.format("%s", data.message()));
        LOGS_EMBED.setTimestamp(OffsetDateTime.now());
        getConsoleChannel().sendMessageEmbeds(LOGS_EMBED.build()).queue();

        if (data.file() != null) {
            final FileUpload FILE_TO_UPLOAD = FileUpload.fromData(data.file(), "logs.txt");
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

    private static void handleMessages(GuildChannel eventChannel, Message message) {
        final User AUTHOR = message.getAuthor();
        if (AUTHOR.isBot()) return;

        if(isConsoleChannel(eventChannel)) {
            message.delete().queue(); // Delete the edited message
            return;
        }

        final boolean IS_LINK = DiscordSecurity.checkLink(message.getContentRaw());
        if (IS_LINK) {
            message.delete().queue(); // Delete the message containing the link
            AUTHOR.openPrivateChannel().queue(pm -> pm.sendMessage(TranslationManager.format(UsersInfo.getLanguage(AUTHOR.getId()).code,"discord.securityMessage.updateMessage")).queue());
        }
    }

    public static boolean isBotInitialized() { return botBuilder != null; }
}
package com.photon.discord;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import com.photon.discord.commands.CommandsManager;
import com.photon.discord.language.Languages;
import com.photon.discord.language.UsersInfo;
import com.photon.network.NetworkDirectories;
import com.photon.network.sql.SQLDiscordProfile;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.TranslationManager;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
        /* Change debug level */
        final Logger JDA_LOGGER = (Logger) LoggerFactory.getLogger("net.dv8tion.jda");
        JDA_LOGGER.setLevel(ch.qos.logback.classic.Level.WARN);
        
        botBuilder = JDABuilder.createDefault(NetworkDirectories.getConfig().discord_bot_token);
        botBuilder.setActivity(Activity.playing(NetworkDirectories.getConfig().bot_activity)); // Set the bot activity
        botBuilder.addEventListeners(new BotEngine());
        botBuilder.addEventListeners(new CommandsManager());
        botBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        botBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        botBuilder.enableIntents(GatewayIntent.GUILD_PRESENCES);
        botBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        botBuilder.build();

        /* Change the restarting status */
        isRestarting = shouldRestart;

        /* Load commands and features */
        CommandsManager.load();
        TranslationManager.loadAllLanguages("lang");

        ConsoleManager.create("Discord Bot connection established").withType(EnumLogType.NETWORK).end();

        Thread.sleep(550); // Wait for the guild to be ready
    }

    /**
     * When the bot is ready, register global slash commands
     */
    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().updateCommands().addCommands(CommandsManager.getGlobalCommands()).queue();
    }

    /**
     * Register slash commands when the bot is ready
     * 
     * @param event The event of the bot being ready
     * @author Mini
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        guild = event.getGuild();
        guild.updateCommands().addCommands(CommandsManager.getGuildCommands()).queue();
        if (isRestarting) {
            guild.getTextChannelById(NetworkDirectories.getConfig().network_console_channel_id).sendMessage("Network restarted").queue();
            isRestarting = false;
        }
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) { CommandsManager.onSlashCommand(event); }

    /**
     * When a user leave the server, remove him from the database
     * 
     * @param event The event of a user leaving the server
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) { SQLDiscordProfile.setLanguages(event.getUser().getId(), new ArrayList<>()); }

    /**
     * When a user get a role
     * 
     * @param event The event of a user getting a role
     * @author Mini
     */
    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        /* When a user get a role, add the language to his profile */
        for (Role role : event.getRoles()){
            // Note : switch case doesn't work with long
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
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        for (Role role : event.getRoles()){
            // Note : switch case doesn't work with long
            if (role.getIdLong() == Roles.FR.id) UsersInfo.removeLanguages(event.getUser().getId(), Languages.FRENCH);
            else if (role.getIdLong() == Roles.EN.id) UsersInfo.removeLanguages(event.getUser().getId(), Languages.ENGLISH);
        }
    }

    /**
     * When a message is updated
     * 
     * @param event The event of a message being update
     */
    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.getAuthor().isBot()) return;

        final boolean IS_LINK = DiscordSecurity.checkLink(event.getMessage().getContentRaw());
        if (IS_LINK) {
            event.getMessage().delete().queue(); // Delete the message containing the link

            event.getAuthor().openPrivateChannel().queue(channel -> {
                channel.sendMessage(TranslationManager.format(UsersInfo.getLanguage(event.getAuthor().getId()).code,
                        "discord.securityMessage.updateMessage")).queue();
            });
        }
    }

    public static boolean isBotInitialized() { return botBuilder != null; }
}
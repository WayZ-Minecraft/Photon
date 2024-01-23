package com.photon.discord;

import java.awt.Color;
import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import org.jetbrains.annotations.NotNull;

import com.photon.discord.slashCommands.AutoCompleteCommands;
import com.photon.discord.slashCommands.SlashCommands;
import com.photon.discord.usersInteraction.MemberJoin;
import com.photon.discord.usersInteraction.Security;
import com.photon.discord.usersInteraction.xpManager;
import com.photon.discord.usersInteraction.data.UsersInfo;
import com.photon.discord.usersInteraction.language.LanguageChoice;
import com.photon.network.NetworkDirectories;
import com.photon.network.sql.SQLuser;
import com.photon.network.sql.SqlInteract;
import com.photon.util.ConsoleManager;
import com.photon.util.TranslationManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
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
    
    public static JDABuilder botBuilder;

    public static Guild guild;
    public static TextChannel botChannel;
    public static boolean isRestarting = false;

    /**
     * Load the bot, register slash commands and start it
     * @throws LoginException
     * @throws InterruptedException
     */
    public static void load(String... args) throws LoginException, InterruptedException {
        String token = NetworkDirectories.config.discordBotToken;
        botBuilder = JDABuilder.createDefault(token);
        botBuilder.setActivity(Activity.playing("/"));
        
        botBuilder.addEventListeners(new BotEngine());
        botBuilder.addEventListeners(new AutoCompleteCommands());
        botBuilder.addEventListeners(new MemberJoin());
        botBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        botBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        botBuilder.enableIntents(GatewayIntent.GUILD_PRESENCES);
        
        botBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);

        if(Arrays.asList(args).contains("--restart")) isRestarting = true;

        botBuilder.build();
        SlashCommands.load();
        SqlInteract.connect();

        TranslationManager.loadAllLanguages("lang");
        Thread.sleep(125);
    }

    /**
     * When the bot is ready, register global slash commands
     */
    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().updateCommands().addCommands(SlashCommands.globalCommand).queue();
    }
    
    /**
     * Register slash commands when the bot is ready
     * @param event The event of the bot being ready
     * @author Mini
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        guild = event.getGuild();
        guild.updateCommands().addCommands(SlashCommands.commands).queue();
        if (isRestarting) {
            guild.getTextChannelById(Channels.CONSOLE_NETWROK.id).sendMessage("Network restarted").queue();
            isRestarting = false;
        }
    }


    /**
     * To log something on discord console manager chanel
     * @param color The color of the log
     * @param title The title of the log
     * @param content The content of the log
     * @param file The file to send with the log (if null, no file is sent)
     */
    public static void log(Color color, String title, Object content, File file) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(color);
        embed.setTitle(title);
        embed.setDescription(content.toString());
        guild.getTextChannelById(Channels.CONSOLE_NETWROK.id).sendMessageEmbeds(embed.build()).queue();

        if (file != null){
            FileUpload uploadfile = FileUpload.fromData(file, "log.txt");
            guild.getTextChannelById(Channels.CONSOLE_NETWROK.id).sendFiles(uploadfile).queue();
        };
    }


    /**
     * Handle slash commands
     * 
     * @param event The event that triggered this command
     * @author Mini
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        SlashCommands.onSlashCommand(event);
    }

    /**
     * When a user leave the server, remove him from the database
     * @param event The event of a user leaving the server
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        try {
            SQLuser.setLanguages(event.getUser().getId(), null);
        } catch (SQLException e) {
            ConsoleManager.create("Error while removing user from database" + e).error().displayOnDiscord().end();
        }
    }


    /**
     * When a user get a role
     * @param event The event of a user getting a role
     */
    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        LanguageChoice.onMemberRoleAdd(event);
        MemberJoin.onMemberRoleAdd(event);
    }


    /**
     * When a user lose a role
     * @param event The event of a user losing a role
     */
    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        LanguageChoice.onMemberRoleRemove(event);
    }

    /**
     * When a message is received
     * @param event The event of a message being received
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        xpManager.onMessageReceived(event);
    }

    /**
     * When a message is updated
     * @param event The event of a message being update
     */
    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.getAuthor().isBot()) return;
        boolean isLink = Security.checkLink(event.getMessage().getContentRaw());
        if (isLink) {
            event.getMessage().delete().queue();

            event.getAuthor().openPrivateChannel().queue((channel) -> {
                channel.sendMessage(TranslationManager.format(UsersInfo.getLanguage(event.getAuthor().getId()).code, "discord.securityMessage.updateMessage")).queue();
            });
        }
    }

}

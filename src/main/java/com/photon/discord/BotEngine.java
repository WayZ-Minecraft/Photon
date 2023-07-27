package com.photon.discord;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import org.jetbrains.annotations.NotNull;

import com.photon.discord.slashCommands.AutoCompleteCommands;
import com.photon.discord.slashCommands.SlashCommands;
import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;

public class BotEngine extends ListenerAdapter {
    
    public static JDABuilder botBuilder;

    public static Guild guild;
    public static TextChannel botChannel;
    public static boolean isRestarting = false;

    /**
     * Load the bot, register slash commands and start it
     * @throws LoginException
     */
    public static void load(String... args) throws LoginException {
        String token = NetworkDirectories.config.discordBotToken;
        botBuilder = JDABuilder.createDefault(token);
        botBuilder.setActivity(Activity.playing("/"));
        
        botBuilder.addEventListeners(new BotEngine());
        botBuilder.addEventListeners(new AutoCompleteCommands());
        botBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);

        if(Arrays.asList(args).contains("--restart")) isRestarting = true;

        botBuilder.build();
        SlashCommands.load();
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
            guild.getTextChannelById(NetworkDirectories.config.discordBotChannelID).sendMessage("Network restarted").queue();
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
        guild.getTextChannelById(NetworkDirectories.config.discordBotChannelID_LOG).sendMessageEmbeds(embed.build()).queue();
        
        if (file != null){
            FileUpload uploadfile = FileUpload.fromData(file, "log.txt");
            guild.getTextChannelById(NetworkDirectories.config.discordBotChannelID_LOG).sendFiles(uploadfile).queue();
        };
    }

    // Test consol manager TODO: remove
    // @Override
    // public void onMessageReceived(net.dv8tion.jda.api.events.message.MessageReceivedEvent event) {
    //     if (event.getAuthor().isBot()) return;
    //     ConsoleManager.create("Discord").displayOnDiscord().withType(ConsoleManager.EnumLogType.INFO).withFile(new File(".gitignore")).end();
    // }

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

}

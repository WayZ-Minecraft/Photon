package com.photon.discord;

import javax.security.auth.login.LoginException;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotEngine extends ListenerAdapter {
    
    public static Guild guild;

    public static void main(String[] args) throws LoginException {
        String token = ""; //TODO : add your token here;
        JDABuilder botBuilder = JDABuilder.createDefault(token);
        botBuilder.setActivity(Activity.playing("/"));
        
        botBuilder.addEventListeners(new BotEngine());
        botBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);

        botBuilder.build();
        SlashCommands.load();
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
        guild.updateCommands().addCommands(SlashCommands.commands).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return; // Ignore messages from other bots
        System.out.println("We received a message from " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay());

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

}

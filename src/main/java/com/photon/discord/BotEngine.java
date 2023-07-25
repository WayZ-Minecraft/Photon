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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotEngine extends ListenerAdapter {
    
    public static void main(String[] args) throws LoginException {
        String token = ""; //TODO : add your token here;
        JDABuilder botBuilder = JDABuilder.createDefault(token);
        botBuilder.setActivity(Activity.playing("/"));
        
        botBuilder.addEventListeners(new BotEngine());
        botBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);

        botBuilder.build();
    }

    /**
     * Register slash commands when the bot is ready
     * @param event The event of the bot being ready
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        Guild guild = event.getGuild();
        guild.updateCommands().addCommands(
            Commands.slash("clear", "clear a number of message").addOption(OptionType.INTEGER, "number", "number of message to delete", false, false),
            Commands.slash("hello", "say hello to the bot")
        ).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return; // Ignore messages from other bots
        System.out.println("We received a message from " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay()
        );
        
    }

    /**
     * Handle slash commands
     * @param event The event that triggered this command
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        final String name = event.getName();
        System.out.println("We received a slash command from " +
                event.getUser().getName() + ": " +
                name
        );
        switch (name) {
            case "hello":
                sayHello(event);
                break;
            case "clear":
                clearMessages(event);
                break;
            default:
                break;
        }

    }


    /**
     * Say hello to the user in the channel (/hello)
     * @param event The event that triggered this command
     */
    protected void sayHello(SlashCommandInteractionEvent event) {
        event.getChannel().sendMessage("Hello, " + event.getUser().getAsMention() + "!").queue();
    }

    /**
     * Clear a number of messages in the channel (/clear number)
     * @param event The event that triggered this command
     */
    protected void clearMessages(SlashCommandInteractionEvent event) {
        int number = event.getOption("number").getAsInt();
        event.getChannel().sendMessage(String.format("Clearing %s messages...", number)).queue();
        event.getChannel().purgeMessages(event.getChannel().getHistory().retrievePast(number).complete());
    }

}

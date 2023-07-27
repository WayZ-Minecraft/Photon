package com.photon.discord.slashCommands;


import java.util.ArrayList;
import java.util.List;

import com.photon.discord.slashCommands.advancedCommands.CustomMute;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class SlashCommands {
    public static List<CommandData> commands = new ArrayList<>();

    /**
     * Load slash commands, to be registered in discord slash commands interface
     * @author Mini
     */
    public static void load() {
        // Add commands here
        commands.add(Commands.slash("hello", "say hello to the bot"));


        // Add commands clear
        commands.add(Commands.slash("clear", "clear a number of message")
        .addOption(OptionType.INTEGER, "number", "number of message to delete", false, false));

        // Add commands time mute
        commands.add(Commands.slash("tempmute", "use to mute a temporaly a specifique player")
        .addOption(OptionType.USER , "user", "the user", true, false)
        .addOption(OptionType.INTEGER, "duration", "time of mute", true, true)
        .addOption(OptionType.STRING, "reason", "the reason of the mute", false, false)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_MUTE_OTHERS)));
    }

    /**
     * Handle slash commands
     * @param event The event that triggered this command
     * @author Mini
     */
    public static void onSlashCommand(SlashCommandInteractionEvent event){
        final String name = event.getName();

        switch (name) {
            case "hello":
                sayHello(event);
                break;
            case "clear":
                clearMessages(event);
                break;
            case "tempmute":
                tempmute(event);
                break;
            default:
                break;
        }
    }

    /**
     * Say hello to the user in the channel (/hello)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void sayHello(SlashCommandInteractionEvent event) {
        event.reply("Hello, " + event.getUser().getAsMention() + "!").queue();
    }


    /**
     * Clear a number of messages in the channel (/clear number)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void clearMessages(SlashCommandInteractionEvent event) {
        final int number = event.getOption("number").getAsInt();
        event.getChannel().purgeMessages(event.getChannel().getHistory().retrievePast(number).complete());
        event.reply(String.format("Clearing %s messages...", number)).queue();
    }

    /**
     * Mute a user for a certain amount of time (/tempmute user days hours minutes)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void tempmute(SlashCommandInteractionEvent event) {
        System.out.println(event.getOption("user"));
        final User player = event.getOption("user").getAsUser();
        final long time = event.getOption("duration").getAsLong();
        CustomMute.timeMute(player, (int) time);
        event.reply(String.format("Mute %s for %s minutes, reason : %s", player.getName(), time, event.getOption("reason").getAsString())).queue();
    }
}

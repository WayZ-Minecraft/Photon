package com.photon.discord.slashCommands;


import java.util.ArrayList;
import java.util.List;

import com.photon.discord.slashCommands.advancedCommands.CustomMute;
import com.photon.discord.usersInteraction.xpManager;
import com.photon.network.NetworkEngine;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ProfileManager;
import com.photon.util.os.ApplicationUtils;

import net.dv8tion.jda.api.Permission;
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
        .addOption(OptionType.INTEGER, "number", "number of message to delete", false, false)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)));

        // Add commands time mute
        commands.add(Commands.slash("tempmute", "use to mute a temporaly a specifique player")
        .addOption(OptionType.USER , "user", "the user", true, false)
        .addOption(OptionType.STRING, "duration", "time of mute", true, true)
        .addOption(OptionType.STRING, "reason", "the reason of the mute", true, false)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_MUTE_OTHERS)));

        // Add commands restart network
        commands.add(Commands.slash("restart-network", "restart the network")
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        // Add commands link-account
        commands.add(Commands.slash("link-account", "link your minecraft account to your discord account")
        .addOption(OptionType.STRING, "uuid", "your WayZ unique user identity (in game)", true, false)
        .addOption(OptionType.STRING, "authkey", "your authentication Key", true, false));

        //add commands Level
        commands.add(Commands.slash("level", "show your level")
        .addOption(OptionType.USER, "user", "the user", false, false));

        //give xp command
        commands.add(Commands.slash("give-xp", "give xp to a player")
        .addOption(OptionType.USER, "user", "the user", true, false)
        .addOption(OptionType.INTEGER, "xp", "the amount of xp", true, false)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        //remove xp command
        commands.add(Commands.slash("remove-xp", "remove xp to a player")
        .addOption(OptionType.USER, "user", "the user", true, false)
        .addOption(OptionType.INTEGER, "xp", "the amount of xp", true, false)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
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
                CustomMute.tempmute(event);
                break;
            case "restart-network":
                restartNetwork(event);
                break;
            case "link-account":
                linkAccount(event);
                break;
            case "level":
                xpManager.levelEmbed(event);
                break;
            case "give-xp":
                xpManager.giveXp(event);
                break;
            case "remove-xp":
                xpManager.removeXp(event);
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
     * Restart the network (/restart-network)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void restartNetwork(SlashCommandInteractionEvent event) {
        event.reply("Restarting network...").queue();
        ApplicationUtils.restart(NetworkEngine.class, "--restart");
    }


    /**
     * Link a discord account to a minecraft account (/link-account uuid authkey)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void linkAccount(SlashCommandInteractionEvent event) {
        final String ingameUUID = event.getOption("uuid").getAsString();
        final String AUTHCODE = String.valueOf(event.getOption("authkey").getAsInt());
        if(!ProfileManager.isAuthCodeValid(ingameUUID, AUTHCODE)) {
            if(!ProfileManager.doesProfileExistByUUID(ingameUUID)) event.reply("Error your User Id is wrong or doesn't exist").queue();
            else event.reply("Error your authentication Key is wrong").queue();
            return;
        }

        ObjectPlayerAccount profile = ProfileManager.getProfileFromUUID(ingameUUID);
        profile.discordID = event.getMember().getId();
        profile.discordAuthCode = AUTHCODE;

        event.reply("Your account has been linked").queue();
    }
}

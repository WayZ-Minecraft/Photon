package com.photon.discord.commands;

import com.photon.util.NetworkOnly;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@NetworkOnly
public class ClearCommand extends AbstractSlashCommand {
    public ClearCommand() {
        super("clear", "Clears messages.");
        this.data().addOption(OptionType.INTEGER, "number", "number of message to delete", false, false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    /**
     * Clear a number of messages in the channel (/clear number)
     * 
     * @param event The event that triggered this command
     * @author Mini
     */
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        final int number = event.getOption("number").getAsInt();
        event.getChannel().purgeMessages(event.getChannel().getHistory().retrievePast(number).complete());
        event.reply(String.format("Clearing %s messages...", number)).queue();
    }
}

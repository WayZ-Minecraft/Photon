package com.photon.discord.commands;

import com.photon.util.NetworkOnly;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * @author Niwer
 */
@NetworkOnly
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class ClearCommand extends AbstractSlashCommand {
    public ClearCommand() {
        super("clear", "Clears messages.");
        this.data().addOption(OptionType.INTEGER, "number", "number of message to delete (1 to 100)", false, false);
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
        if (number < 1 || number > 100) {
            event.reply("Please provide a number between 1 and 100.").setEphemeral(true).queue();
            return;
        }

        event.getChannel().purgeMessages(event.getChannel().getHistory().retrievePast(number).complete());
        event.reply(String.format("Clearing %s messages...", number)).setEphemeral(true).queue();
    }
}

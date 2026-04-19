package niwer.photon.discord.commands;

import niwer.photon.util.TranslationManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * @author Niwer
 */
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class ClearCommand extends AbstractSlashCommand {
    public ClearCommand() {
        super("clear", "Clears messages.");
        this.addOption(OptionType.INTEGER, "number", "number of message to delete (1 to 100)", false);
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
        final String USER_ID = event.getUser().getId();
        final int NUMBER = event.getOption("number").getAsInt();
        if (NUMBER < 1 || NUMBER > 100) {
            event.reply(TranslationManager.format(USER_ID, "command.reply.clear.failure")).setEphemeral(true).queue();
            return;
        }

        event.getChannel().purgeMessages(event.getChannel().getHistory().retrievePast(NUMBER).complete());
        event.reply(TranslationManager.format(USER_ID, "command.reply.clear.success", NUMBER)).setEphemeral(true).queue();
    }
}


package niwer.photon.discord.commands.network.accounts;

import niwer.photon.discord.commands.AbstractSlashCommand;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.util.TranslationManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

/**
 * @author Niwer
 */
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class DeleteAllAccountCommand extends AbstractSlashCommand {
    
    public DeleteAllAccountCommand() {
        super("delete-accounts", "Deletes all accounts.");
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        if (!isOfficialGuild(event)) return; // Check if we're on the official guild
        if (!isConsoleChannel(event)) return; // Check if we're in the console channel

        for (ObjectPlayerAccount account : PlayerAccountTable.getAllAccounts()) PlayerAccountTable.deleteAccount(account.getUuid());
        event.reply(TranslationManager.format(event.getUser().getId(), "command.reply.all_account.success")).queue();
    }
}

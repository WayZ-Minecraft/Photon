package niwer.photon.discord.commands.network.accounts;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

import niwer.photon.discord.commands.AbstractSlashCommand;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.util.TranslationManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.utils.FileUpload;

/**
 * @author Niwer
 */
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class ListAccountsCommand extends AbstractSlashCommand {

    public ListAccountsCommand() {
        super("list-accounts", "List all existing accounts.");
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    /**
     * Clear a number of messages in the channel (/clear number)
     * 
     * @param event The event that triggered this command
     * @author Mini
     */
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        if (!isOfficialGuild(event)) return; // Check if we're on the official guild
        if (!isConsoleChannel(event)) return; // Check if we're in the console channel

        /* Get all accounts, and skip if there are no accounts */
        final List<ObjectPlayerAccount> accounts = PlayerAccountTable.getAllAccounts();
        final String USER_ID = event.getUser().getId();
        if (accounts.isEmpty()) {
            event.reply(TranslationManager.format(USER_ID, "command.reply.list_accounts.failure.empty")).setEphemeral(true).queue();
            return;
        }

        /* Create and send the file */
        final StringJoiner joiner = new StringJoiner(System.lineSeparator(), "", System.lineSeparator());
        for (ObjectPlayerAccount account : accounts) joiner.add(formatAccount(account));

        final FileUpload upload = FileUpload.fromData(joiner.toString().getBytes(StandardCharsets.UTF_8), "account.txt");
        event.reply(TranslationManager.format(USER_ID, "command.reply.list_accounts.success", accounts.size())).addFiles(upload).queue();
    }
    
    public static String formatAccount(ObjectPlayerAccount account) {
        return "- " + account.getEmail() + " " + account.getUsername() + " " + account.getUuid() + " " + (account.isAdministrator() ? "He's Project Creator" : "He's not a Project Creator");
    }
}
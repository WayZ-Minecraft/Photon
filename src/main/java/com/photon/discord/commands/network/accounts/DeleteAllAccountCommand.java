package com.photon.discord.commands.network.accounts;

import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;
import com.photon.util.NetworkOnly;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

/**
 * @author Niwer
 */
@NetworkOnly
public class DeleteAllAccountCommand extends AbstractSlashCommand {
    
    public DeleteAllAccountCommand() {
        super("delete-accounts", "Deletes all accounts.");
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        if (!isOfficialGuild(event)) return; // Check if we're on the official guild
        if (!isConsoleChannel(event)) return; // Check if we're in the console channel

        for (ObjectPlayerAccount account : SQLPlayerAccount.getAllAccounts()) SQLPlayerAccount.deleteAccount(account.uuid);
        event.reply("All accounts have been deleted !").queue();
    }
}

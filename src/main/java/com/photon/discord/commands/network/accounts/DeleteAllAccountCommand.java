package com.photon.discord.commands.network.accounts;

import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public class DeleteAllAccountCommand extends AbstractSlashCommand {
    
    public DeleteAllAccountCommand() {
        super("delete-accounts", "Deletes all accounts.");
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        for (ObjectPlayerAccount account : SQLPlayerAccount.getAllAccounts())
            SQLPlayerAccount.deleteAccount(account.uuid);

        event.reply("All accounts have been deleted !").queue();
    }
}

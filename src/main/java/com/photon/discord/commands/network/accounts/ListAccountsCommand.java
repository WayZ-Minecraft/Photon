package com.photon.discord.commands.network.accounts;

import java.util.List;
import java.util.StringJoiner;

import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.utils.FileUpload;

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
        List<ObjectPlayerAccount> list = SQLPlayerAccount.getAllAccounts();
        if (list.isEmpty())
            event.reply("No accounts found").queue();

        StringJoiner joiner = new StringJoiner(System.lineSeparator(), "", System.lineSeparator());
        for (ObjectPlayerAccount account : list) {
            if (account == null)
                continue;
            joiner.add("{").add(account.email).add(account.username).add(account.uuid).add("}");
        }

        FileUpload upload = FileUpload.fromData(joiner.toString().getBytes(), "account.txt");
        event.reply("List of all accounts").addFiles(upload).queue();
    }
}
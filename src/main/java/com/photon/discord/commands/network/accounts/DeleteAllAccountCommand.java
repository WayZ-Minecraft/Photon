package com.photon.discord.commands.network.accounts;

import com.photon.discord.BotEngine;
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
        /* If we're not on the official guild */
        if(!BotEngine.isOfficialGuild(event.getGuild())) {
            event.reply("You're not on the official guild.").setEphemeral(true).queue();
            return;
        }

        /* If we're not in the console */
        if(!BotEngine.isConsoleChannel(event.getGuildChannel())) {
            event.reply("This command can only be used in the console channel.").setEphemeral(true).queue();
            return;
        }

        for (ObjectPlayerAccount account : SQLPlayerAccount.getAllAccounts()) SQLPlayerAccount.deleteAccount(account.uuid);
        event.reply("All accounts have been deleted !").queue();
    }
}

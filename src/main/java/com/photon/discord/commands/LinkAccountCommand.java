package com.photon.discord.commands;

import com.photon.network.sql.SQLPlayerAccount;
import com.photon.util.NetworkOnly;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * @author Niwer
 */
@NetworkOnly
public class LinkAccountCommand extends AbstractSlashCommand {

    public LinkAccountCommand() {
        super("link-account", "Link your Game account to your Discord account.");
        this.data().addOption(OptionType.STRING, "uuid", "Your unique user identity", true, false);
        this.data().addOption(OptionType.STRING, "code", "Your discord auth code.", true, false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        final String UUID = event.getOption("uuid").getAsString();
        final String AUTHCODE = event.getOption("code").getAsString();

        /* Check the UUID and the code */
        if (!SQLPlayerAccount.isAuthCodeValid(UUID, AUTHCODE)) {
            if (!SQLPlayerAccount.existByUUID(UUID)) event.reply("There's no user with this UUID").setEphemeral(true).queue();
            else event.reply("Error your authentication key is wrong").setEphemeral(true).queue();
            return;
        }

        /* Check if the account has already been linked */
        if (SQLPlayerAccount.getAccountByUUID(UUID).hasDiscordLinked()) {
            event.reply("This Game account is already linked to a Discord account.").setEphemeral(true).queue();
            return;
        }

        /* Check if the discord account has already been link to an other official account */
        final String DISCCORD_USER_ID = event.getUser().getId();
        if (SQLPlayerAccount.getAccountByDiscordID(DISCCORD_USER_ID) != null) {
            event.reply("Your Discord account is already linked to another Game account.").setEphemeral(true).queue();
            return;
        }

        /* Update the Discord ID */
        SQLPlayerAccount.updateDiscordID(UUID, DISCCORD_USER_ID);

        /* Print reply */
        event.reply("Your account has been linked to " + event.getUser().getAsMention()).queue();
    }

    @Override public boolean isGlobal() { return true; }
}

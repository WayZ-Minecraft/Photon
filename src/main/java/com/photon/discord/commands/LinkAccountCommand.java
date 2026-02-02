package com.photon.discord.commands;

import com.photon.network.sql.SQLPlayerAccount;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class LinkAccountCommand extends AbstractSlashCommand {

    public LinkAccountCommand() {
        super("link-account", "Link your Niwer's EngineOfficial account to your Discord account.");
        this.data().addOption(OptionType.STRING, "uuid", "Your unique user identity", true, false);
        this.data().addOption(OptionType.STRING, "code", "Your discord auth code.", true, false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        final String UUID = event.getOption("uuid").getAsString();
        final String AUTHCODE = event.getOption("code").getAsString();

        /* Check the UUID and the code */
        // if (!SQLPlayerAccount.isAuthCodeValid(UUID, AUTHCODE)) {
        //     if (!SQLPlayerAccount.existByUUID(UUID)) event.reply("There's no user with this UUID").queue();
        //     else event.reply("Error your authentication key is wrong").queue();
        //     return;
        // }

        // if() // Already linked check can be added here

        /* Update the Discord ID */
        // SQLPlayerAccount.updateDiscordID(UUID, event.getUser().getId());
        SQLPlayerAccount.updateDiscordID(UUID, "Test");

        /* Print reply */
        event.reply("Your account has been linked to " + event.getUser().getAsMention()).queue();
    }

    @Override public boolean isGlobal() { return true; }
}


package com.photon.discord.commands;

import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.sql.PlayerAccountTable;
import com.photon.util.NetworkOnly;
import com.photon.util.TranslationManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * @author Niwer
 */
@NetworkOnly
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class LinkAccountCommand extends AbstractSlashCommand {

    public LinkAccountCommand() {
        super("link-account", "Link your Game account to your Discord account.");
        this.addOption(OptionType.STRING, "uuid", "Your unique user identity", true);
        this.addOption(OptionType.STRING, "code", "Your discord auth code.", true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        final String UUID = event.getOption("uuid").getAsString(); // Won't be null because it's required
        final String AUTHCODE = event.getOption("code").getAsString(); // Won't be null because it's required

        /* Check the UUID and the code */
        if (!PlayerAccountTable.isAuthCodeValid(UUID, AUTHCODE)) {
            if (!PlayerAccountTable.existByUUID(UUID)) event.reply(TranslationManager.format(event.getUser().getId(), "command.link_account.failure.no_uuid")).setEphemeral(true).queue();
            else event.reply(TranslationManager.format(event.getUser().getId(), "command.link_account.failure.invalid_code")).setEphemeral(true).queue();
            return;
        }

        /* Check if there's an account with this UUID */
        final ObjectPlayerAccount profile = PlayerAccountTable.getAccountByUUID(UUID);
        if (profile == null) {
            event.reply(TranslationManager.format(event.getUser().getId(), "command.link_account.failure.no_uuid")).setEphemeral(true).queue();
            return;
        }

        /* Check if the account has already been linked */
        if (profile.hasDiscordLinked()) {
            event.reply(TranslationManager.format(event.getUser().getId(), "command.link_account.failure.already_linked")).setEphemeral(true).queue();
            return;
        }

        /* Check if the discord account has already been linked to another official account */
        final String DISCORD_USER_ID = event.getUser().getId();
        if (PlayerAccountTable.getAccountByDiscordID(DISCORD_USER_ID) != null) {
            event.reply(TranslationManager.format(event.getUser().getId(), "command.link_account.failure.linked_to_other")).setEphemeral(true).queue();
            return;
        }

        /* Update the Discord ID */
        PlayerAccountTable.updateDiscordID(UUID, DISCORD_USER_ID);

        /* Auto-assign ServerCreator role if applicable */ //TODO
        // if (profile.serverCreator && BotEngine.guild != null) {
        //     final Role serverCreatorRole = BotEngine.guild.getRoleById(1474183624134758525L);
        //     if (serverCreatorRole != null) {
        //         BotEngine.guild.retrieveMemberById(DISCORD_USER_ID).queue(
        //             member -> BotEngine.guild.addRoleToMember(member, serverCreatorRole).queue(),
        //             err -> {}
        //         );
        //     }
        // }

        /* Print reply */
        event.reply(TranslationManager.format(event.getUser().getId(), "command.link_account.success", event.getUser().getAsMention())).queue();
    }

    @Override public boolean isGlobal() { return true; }
}
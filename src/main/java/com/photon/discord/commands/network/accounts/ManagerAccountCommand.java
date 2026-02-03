package com.photon.discord.commands.network.accounts;

import java.nio.charset.StandardCharsets;

import com.photon.discord.BotEngine;
import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ManagerAccountCommand extends AbstractSlashCommand {

    public ManagerAccountCommand() {
        super("account", "Manage a single account (Get data, Edit data, Delete account).");
        this.addOption(OptionType.STRING, "action", "Can be get, edit or delete", true, ActionType.class);
        this.addOption(OptionType.STRING, "field_type", "Choose the type of field to search by (UUID, DISCORD_ID, EMAIL, USERNAME)", true, FieldType.class);
        this.addOption(OptionType.STRING, "field_value", "Enter an email/uuid/discord_id/username to get the account you want to manager", true);
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

        final FieldType FIELD = FieldType.valueOf(event.getOption("field_type").getAsString().toUpperCase());
        final String VALUE = event.getOption("field_value").getAsString();
        final ObjectPlayerAccount PROFILE = switch (FIELD) {
            case DISCORD_ID -> SQLPlayerAccount.getAccountByDiscordID(VALUE);
            case UUID -> SQLPlayerAccount.getAccountByUUID(VALUE);
            case EMAIL -> SQLPlayerAccount.getAccountByEmail(VALUE);
            case USERNAME -> SQLPlayerAccount.getAccountByUsername(VALUE);
        };

        manageAccount(event, PROFILE, event.getOption("action").getAsString());
    }

    private static void manageAccount(SlashCommandInteractionEvent event, ObjectPlayerAccount profile, String action) {
        /* If the profile doesn't exist */
        if (profile == null) {
            event.reply("Error, the profile doesn't exist").setEphemeral(true).queue();
            return;
        }

        /* Ensure the action is valid */
        if(action == null || action.isEmpty()) {
            event.reply("An error occurred while processing your request.").setEphemeral(true).queue();
            return;
        }

        final ActionType ACTION = ActionType.valueOf(action.toUpperCase());
        switch (ACTION) {
            case GET:
                event.reply("Here's the account information for the required account : " + ListAccountsCommand.formatAccount(profile).getBytes(StandardCharsets.UTF_8)).queue();
                break;
            case EDIT:
                event.reply("Not supported yet. Use the SQL Command.").setEphemeral(true).queue();
                break;
            case DELETE:
                SQLPlayerAccount.deleteAccount(profile.uuid);
                event.reply("The profile has been deleted !").queue();
                break;
        }
    }

    private static enum ActionType {
        GET,
        EDIT,
        DELETE
    }

    private static enum FieldType {
        UUID,
        DISCORD_ID,
        EMAIL,
        USERNAME
    }
}

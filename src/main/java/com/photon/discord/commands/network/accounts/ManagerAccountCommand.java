package com.photon.discord.commands.network.accounts;

import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;
import com.photon.util.ConsoleManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ManagerAccountCommand extends AbstractSlashCommand {

    public ManagerAccountCommand() {
        super("account", "Manager accounts");
        this.data().addOption(OptionType.STRING, "type", "can be : uuid, discordid, email or username", true, false);
        this.data().addOption(OptionType.STRING, "getter", "The email/username/... according to the type", true, false);
        this.data().addOption(OptionType.STRING, "action", "Can be get, edit or delete", true, false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String type = event.getOption("type").getAsString();
        String getter = event.getOption("getter").getAsString();
        ObjectPlayerAccount profile = null;
        switch (type.toLowerCase()) {
            case "discordid":
                profile = SQLPlayerAccount.getAccountByDiscordID(getter);
                break;
            case "uuid":
                profile = SQLPlayerAccount.getAccountByUUID(getter);
                break;
            case "email":
                profile = SQLPlayerAccount.getAccountByEmail(getter);
                ConsoleManager.debug(getter);
                break;
            case "username":
                profile = SQLPlayerAccount.getAccountByUsername(getter);
                break;
        }
        managerAccount(event, profile, event.getOption("action").getAsString());
    }

    private static void managerAccount(SlashCommandInteractionEvent event, ObjectPlayerAccount profile, String action) {
        if (profile == null) {
            event.reply("Error, the profile doesn't exist").queue();
            return;
        }
        switch (action.toLowerCase()) {
            case "get":
                // FileUpload upload = FileUpload.fromData(ProfileManager.getGson().toJson(profile).getBytes(),
                //         profile.username + ".json");
                //TODO We were sending the profile as JSON file but this could be a data privacy/leaks issue
                event.reply("The info for the user : " + profile.username + "(" + profile.email + ")")/* .addFiles(upload) */
                        .queue();
                break;
            case "edit":
                break;
            case "delete":
                SQLPlayerAccount.deleteAccount(profile.uuid);
                event.reply("The profile has been deleted !").queue();
                break;
        }
    }
}

package niwer.photon.discord.commands.network.accounts;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import niwer.photon.discord.commands.AbstractSlashCommand;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PlayerAccountTable;

/**
 * Command to add or remove the Administrator status to a user.
 * 
 * @author Niwer
 */
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class AdministratorCommand extends AbstractSlashCommand {

    private enum EnumActionType {
        ADD, REMOVE;

        public static EnumActionType fromString(String str) {
            for (final EnumActionType TYPE : EnumActionType.values()) {
                if (TYPE.name().equalsIgnoreCase(str)) return TYPE;
            }
            throw new IllegalArgumentException("Invalid action type: " + str);
        }
    }

    public AdministratorCommand() {
        super("administrator", "Add or remove Administrator status to a user.");
        this.addOption(OptionType.STRING, "action", "add or remove", true, EnumActionType.class); // Discord doesn't support enum options, so we'll validate manually in code
        this.addOption(OptionType.USER, "discord_user", "Target Discord user (if discord account is linked to an ingame account)", false);
        this.addOption(OptionType.STRING, "username", "Target ingame username (if not linked to any discord account)", false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        if (!isOfficialGuild(event)) return;

        final String GIVEN_ACTION_CONTENT = event.getOption("action").getAsString(); // Won't be null because it's required
        final EnumActionType ACTION = EnumActionType.fromString(GIVEN_ACTION_CONTENT);
        final boolean IS_ADD_ACTION = ACTION == EnumActionType.ADD;

        /* Resolve account — by Discord user or by username */
        final OptionMapping DISCORD_USER_OPTION = event.getOption("discord_user");
        final OptionMapping USERNAME_OPTION = event.getOption("username");
        if(DISCORD_USER_OPTION == null && USERNAME_OPTION == null) {
            event.reply("You must provide either a Discord user or a username.").setEphemeral(true).queue();
            return;
        }

        /* Try to get the player account */
        final ObjectUserAccount PLAYER_ACCOUNT = DISCORD_USER_OPTION != null ? PlayerAccountTable.getAccountByDiscordID(DISCORD_USER_OPTION.getAsUser().getId()) : PlayerAccountTable.getAccountByUsername(USERNAME_OPTION.getAsString());
        if (PLAYER_ACCOUNT == null) {
            event.reply("No account found for the provided account.").setEphemeral(true).queue();
            return;
        }
        
        /* Update administrator in DB */
        PlayerAccountTable.setAdministrator(PLAYER_ACCOUNT.getUuid(), IS_ADD_ACTION);
        
        /* If there's no Discord link, only update the database */
        if (!PLAYER_ACCOUNT.hasDiscordLinked()) {
            event.reply((IS_ADD_ACTION ? "Added" : "Removed") + " Project Author for **" + PLAYER_ACCOUNT.getUsername() + "** (no Discord linked, only DB updated).").queue();
            return;
        }
    }
}
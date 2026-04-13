package com.photon.discord.commands.network.accounts;

import javax.annotation.Nonnull;

import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.NetworkDirectories;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.sql.PlayerAccountTable;
import com.photon.util.NetworkOnly;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Command to add or remove the Server Creator role and status to a user.
 * The role ID is configured via the network config file (server_creator_role_id).
 * 
 * @author Niwer
 */
@NetworkOnly
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class ServerCreatorCommand extends AbstractSlashCommand {

    private enum EnumActionType {
        ADD, REMOVE;

        public static EnumActionType fromString(String str) {
            for (final EnumActionType TYPE : EnumActionType.values()) {
                if (TYPE.name().equalsIgnoreCase(str)) return TYPE;
            }
            throw new IllegalArgumentException("Invalid action type: " + str);
        }
    }

    public ServerCreatorCommand() {
        super("server_creator", "Add or remove Server Creator status to a user.");
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
        final ObjectPlayerAccount PLAYER_ACCOUNT = DISCORD_USER_OPTION != null ? PlayerAccountTable.getAccountByDiscordID(DISCORD_USER_OPTION.getAsUser().getId()) : PlayerAccountTable.getAccountByUsername(USERNAME_OPTION.getAsString());
        if (PLAYER_ACCOUNT == null) {
            event.reply("No account found for the provided account.").setEphemeral(true).queue();
            return;
        }
        
        /* Update serverCreator in DB */
        PlayerAccountTable.setServerCreator(PLAYER_ACCOUNT.uuid, IS_ADD_ACTION);
        
        /* If there's no Discord link, only update the database */
        if (!PLAYER_ACCOUNT.hasDiscordLinked()) {
            event.reply((IS_ADD_ACTION ? "Added" : "Removed") + " Server Creator for **" + PLAYER_ACCOUNT.username + "** (no Discord linked, only DB updated).").queue();
            return;
        }
        
        /* Update Discord role if the account has a linked Discord ID */
        final String ROLE_ID = NetworkDirectories.getConfig().server_creator_role_id;
        @Nonnull final Guild GUILD = event.getGuild();

        /* Get the discord role */
        final Role ROLE = GUILD.getRoleById(ROLE_ID);
        if (ROLE == null) {
            event.reply("Server Creator role not found on the official server (ID: " + ROLE_ID + ").").setEphemeral(true).queue();
            return;
        }

        /* Try to update the Discord role */
        GUILD.retrieveMemberById(PLAYER_ACCOUNT.discordID).queue(member -> updateDiscordRole(GUILD, member, ROLE, IS_ADD_ACTION), err -> event.reply("Discord user not found (ID: " + PLAYER_ACCOUNT.discordID + ").").setEphemeral(true).queue());
    }

    private static void updateDiscordRole(Guild guild, Member member, Role role, boolean shouldAdd) {
        final var ACTION = shouldAdd ? guild.addRoleToMember(member, role) : guild.removeRoleFromMember(member, role);
        ACTION.queue(s -> System.out.println("Role " + (shouldAdd ? "added" : "removed") + " successfully."), err -> System.out.println("Failed to modify role: " + err.getMessage()));
    }
}
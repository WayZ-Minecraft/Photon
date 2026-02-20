package com.photon.discord.commands.network.accounts;

import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.NetworkDirectories;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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
public class ServerCreatorCommand extends AbstractSlashCommand {

    private enum ActionType { ADD, REMOVE }

    public ServerCreatorCommand() {
        super("server_creator", "Add or remove Server Creator status to a user.");
        this.addOption(OptionType.STRING, "action", "add or remove", true, ActionType.class);
        this.addOption(OptionType.USER, "discord_user", "Target Discord user (if account is linked)", false);
        this.addOption(OptionType.STRING, "username", "Target username in PlayerAccount (if not linked)", false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        if (!isOfficialGuild(event)) return;

        final OptionMapping actionOption = event.getOption("action");
        if (actionOption == null) {
            event.reply("You must provide an action (add or remove).").setEphemeral(true).queue();
            return;
        }

        final ActionType action = ActionType.valueOf(actionOption.getAsString().toUpperCase());
        final boolean isAdd = action == ActionType.ADD;

        /* Resolve account — by Discord user or by username */
        final OptionMapping discordUserOption = event.getOption("discord_user");
        final OptionMapping usernameOption = event.getOption("username");

        final ObjectPlayerAccount resolved;
        if (discordUserOption != null) {
            resolved = SQLPlayerAccount.getAccountByDiscordID(discordUserOption.getAsUser().getId());
            if (resolved == null) {
                event.reply("This Discord user has not linked their account.").setEphemeral(true).queue();
                return;
            }
        } else if (usernameOption != null) {
            resolved = SQLPlayerAccount.getAccountByUsername(usernameOption.getAsString());
            if (resolved == null) {
                event.reply("No account found with this username.").setEphemeral(true).queue();
                return;
            }
        } else {
            event.reply("You must provide either a Discord user or a username.").setEphemeral(true).queue();
            return;
        }

        /* Update serverCreator in DB */
        SQLPlayerAccount.setServerCreator(resolved.uuid, isAdd);

        /* Update Discord role if the account has a linked Discord ID */
        if (resolved.discordID != null && !resolved.discordID.isBlank()) {
            final String roleIdStr = NetworkDirectories.getConfig().server_creator_role_id;
            if (roleIdStr == null || roleIdStr.isBlank()) {
                event.reply("Server Creator role ID is not configured (server_creator_role_id in config).").setEphemeral(true).queue();
                return;
            }

            final Guild guild = event.getGuild();
            final Role role = guild.getRoleById(roleIdStr);
            if (role == null) {
                event.reply("Server Creator role not found on this server (ID: " + roleIdStr + ").").setEphemeral(true).queue();
                return;
            }

            guild.retrieveMemberById(resolved.discordID).queue(
                member -> {
                    if (isAdd) {
                        guild.addRoleToMember(member, role).queue(
                            s -> event.reply("**" + resolved.username + "** is now a Server Creator.").queue(),
                            e -> event.reply("DB updated but failed to assign role: " + e.getMessage()).setEphemeral(true).queue()
                        );
                    } else {
                        guild.removeRoleFromMember(member, role).queue(
                            s -> event.reply("**" + resolved.username + "** is no longer a Server Creator.").queue(),
                            e -> event.reply("DB updated but failed to remove role: " + e.getMessage()).setEphemeral(true).queue()
                        );
                    }
                },
                err -> event.reply("DB updated but Discord user not found on server (ID: " + resolved.discordID + ").").setEphemeral(true).queue()
            );
        } else {
            event.reply((isAdd ? "Added" : "Removed") + " Server Creator for **" + resolved.username + "** (no Discord linked, only DB updated).").queue();
        }
    }
}
package com.photon.discord.commands.network;

import java.sql.SQLException;

import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.sql.SQLInteraction;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ExecuteSQLCommand extends AbstractSlashCommand {

    public ExecuteSQLCommand() {
        super("execute-sql", "Execute a SQL command.");
        this.data().addOption(OptionType.STRING, "command", "SQL command to execute", true, false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String command = event.getOption("command").getAsString();
        try {
            String result = SQLInteraction.executeSQLCommandToArray(command);
            event.reply(result).queue();
        } catch (SQLException e) {
            event.reply("Error with Sql commande :" + e).queue();
        } catch (Exception e) {
            event.reply(e.toString()).queue();
        }
    }
}

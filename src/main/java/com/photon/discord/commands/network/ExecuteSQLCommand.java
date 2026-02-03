package com.photon.discord.commands.network;

import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;

import com.photon.discord.BotEngine;
import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.sql.SQLInteraction;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ExecuteSQLCommand extends AbstractSlashCommand {

    public ExecuteSQLCommand() {
        super("execute-sql", "Execute a SQL command.");
        this.addOption(OptionType.STRING, "command", "SQL command to execute", false);
        this.addOption(OptionType.ATTACHMENT, "sql_script", "SQL file to execute", false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
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

        final OptionMapping SQL_COMMAND_ARG = event.getOption("command");
        if(SQL_COMMAND_ARG != null) {
            executeSQL(event, SQL_COMMAND_ARG.getAsString());
            return;
        }

        final var SQL_ATTACHMENT_ARG = event.getOption("sql_script");
        if(SQL_ATTACHMENT_ARG != null) {
            final var ATTACHMENT = SQL_ATTACHMENT_ARG.getAsAttachment();
            if(ATTACHMENT.getFileName().endsWith(".sql")) {
                try {
                    final URL SCRIPT_URL = new URL(ATTACHMENT.getUrl());
                    final URLConnection CONNECTION = SCRIPT_URL.openConnection();
    
                    final StringBuilder connectionContent = new StringBuilder();
                    try (var in = CONNECTION.getInputStream(); var reader = new java.io.BufferedReader(new java.io.InputStreamReader(in))) {
                        String line;
                        while ((line = reader.readLine()) != null) connectionContent.append(line).append("\n");
                    }
    
                    executeSQL(event, connectionContent.toString());
                } catch (Exception e) {
                    event.reply("Failed to retrieve SQL script: " + e.getMessage()).setEphemeral(true).queue();
                }
                return;
            }

            /* If the file isn't a .sql script */
            event.reply("The provided file is not a .sql script").setEphemeral(true).queue();
            return;
        }

        /* If there are no SQL Script neither command provided */
        event.reply("No SQL command or script provided.").setEphemeral(true).queue();
    }

    private void executeSQL(SlashCommandInteractionEvent event, String command) {
        try {
            final String SQL_RESULT_AS_ARRAY = SQLInteraction.executeSQLCommandToArray(command, true);
            event.reply(SQL_RESULT_AS_ARRAY).queue();
        } catch (SQLException sqlEx) {
            event.reply("Error with SQL command :" + sqlEx).setEphemeral(true).queue();
        } catch (Exception ex) {
            event.reply(ex.toString()).setEphemeral(true).queue();
        }
    }
}

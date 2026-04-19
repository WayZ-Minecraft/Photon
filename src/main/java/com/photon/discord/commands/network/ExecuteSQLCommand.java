package com.photon.discord.commands.network;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.photon.PhotonEngine;
import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.util.NetworkOnly;
import com.photon.util.TranslationManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@NetworkOnly
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class ExecuteSQLCommand extends AbstractSlashCommand {

    public ExecuteSQLCommand() {
        super("execute-sql", "Execute a SQL command.");
        this.addOption(OptionType.STRING, "command", "SQL command to execute", false);
        this.addOption(OptionType.ATTACHMENT, "sql_script", "SQL file to execute", false);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        if (!isOfficialGuild(event)) return; // Check if we're on the official guild
        if (!isConsoleChannel(event)) return; // Check if we're in the console channel

        final OptionMapping SQL_COMMAND_ARG = event.getOption("command");
        if(SQL_COMMAND_ARG != null) {
            executeSQL(event, SQL_COMMAND_ARG.getAsString());
            return;
        }

        final var SQL_ATTACHMENT_ARG = event.getOption("sql_script");
        final String USER_ID = event.getUser().getId();
        if(SQL_ATTACHMENT_ARG != null) {
            final var ATTACHMENT = SQL_ATTACHMENT_ARG.getAsAttachment();
            if(ATTACHMENT.getFileName().endsWith(".sql")) {
                try {
                    final URL SCRIPT_URL = new URI(ATTACHMENT.getUrl()).toURL();
                    final URLConnection CONNECTION = SCRIPT_URL.openConnection();
    
                    final StringBuilder connectionContent = new StringBuilder();
                    try (var in = CONNECTION.getInputStream(); var reader = new java.io.BufferedReader(new java.io.InputStreamReader(in))) {
                        String line;
                        while ((line = reader.readLine()) != null) connectionContent.append(line).append("\n");
                    }
    
                    executeSQL(event, connectionContent.toString());
                } catch (Exception e) {
                    event.reply(TranslationManager.format(USER_ID, "command.reply.sql.failure.script", e.getMessage())).setEphemeral(true).queue();
                }
                return;
            }

            /* If the file isn't a .sql script */
            event.reply(TranslationManager.format(USER_ID, "command.reply.sql.failure.wrong_file")).setEphemeral(true).queue();
            return;
        }

        /* If there are no SQL Script neither command provided */
        event.reply(TranslationManager.format(USER_ID, "command.reply.sql.failure")).setEphemeral(true).queue();
    }

    private void executeSQL(SlashCommandInteractionEvent event, String command) {
        try {
            final String SQL_RESULT_AS_ARRAY = executeSQLCommandToArray(command, true);
            event.reply(SQL_RESULT_AS_ARRAY).queue();
        } catch (Exception ex) {
            event.reply(TranslationManager.format(event.getUser().getId(), "command.reply.sql.failure.query", ex.getMessage())).setEphemeral(true).queue();
        }
    }

    /**
     * This command will execute an SQL command and return the result as a formatted string.
     * @param command The SQL command to execute
     * @return Formatted result string with columns separated by " | "
     * @throws SQLException if query execution fails
     */
    private static String executeSQLCommandToArray(String command, boolean markdown) throws SQLException {
        PhotonEngine.DATA_BASE.connect(); // Ensure we're connected to the database

        final Statement STATEMENT = PhotonEngine.DATA_BASE.sqlConnection().createStatement();
        final ResultSet RESULT = STATEMENT.executeQuery(command);
        final int COLUMN_COUNT = RESULT.getMetaData().getColumnCount();
        final int[] COLUMNS_WIDTHS = new int[COLUMN_COUNT]; // E.G : [5, 10, 3] for 3 columns
        final StringBuilder BUILDER = new StringBuilder();
        final List<String[]> ROWS = new ArrayList<>(); // List of rows, each row is an array of strings (The array represents the fields for each column)
        
        /* Calculate column size and save create rows with values for each column */
        for (int columnID = 0; columnID < COLUMN_COUNT; columnID++) {
            String columnName = RESULT.getMetaData().getColumnName(columnID + 1);
            if (columnName == null) columnName = "";
            if (columnName.length() > COLUMNS_WIDTHS[columnID]) COLUMNS_WIDTHS[columnID] = columnName.length();
        }
        while (RESULT.next()) {
            String[] row = new String[COLUMN_COUNT];
            for (int columnID = 0; columnID < COLUMN_COUNT; columnID++) {
                String field = RESULT.getString(columnID + 1);
                if (field == null) field = ""; // If null, set empty string
                
                row[columnID] = field; // Save field in the row
                if (field.length() > COLUMNS_WIDTHS[columnID]) COLUMNS_WIDTHS[columnID] = field.length(); // If the field is wider than the current width, set it
            }
            ROWS.add(row);
        }
        
        /* Create the board (if markdown is set to true, then we'll print with markdown formatting) */
        {
            if (markdown) BUILDER.append("```\n");
            /* Title row */
            BUILDER.append("| ");
            for (int i = 0; i < COLUMN_COUNT; i++) {
                String columnName = RESULT.getMetaData().getColumnName(i + 1);
                if (columnName == null) columnName = "";
                BUILDER.append(String.format("%-" + COLUMNS_WIDTHS[i] + "s", columnName));
                BUILDER.append(" | ");
            }
            BUILDER.append("\n|");
            for (int i = 0; i < COLUMN_COUNT; i++) BUILDER.append(" ").append("-".repeat(COLUMNS_WIDTHS[i])).append(" |");
            BUILDER.append("\n");
            
            /* Data rows */
            for (final String[] ROW : ROWS) {
                BUILDER.append("| ");
                for (int i = 0; i < COLUMN_COUNT; i++) {
                    final String VALUE = ROW[i];
                    BUILDER.append(String.format("%-" + COLUMNS_WIDTHS[i] + "s", VALUE)).append(" | ");
                }
                BUILDER.append("\n");
            }
            if (markdown) BUILDER.append("```");
        }
        
        STATEMENT.close();
        RESULT.close();

        PhotonEngine.DATA_BASE.disconnect();

        return new String(BUILDER.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}

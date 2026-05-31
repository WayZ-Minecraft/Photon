package niwer.photon.discord.commands.network;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import niwer.photon.PhotonEngine;
import niwer.photon.discord.commands.AbstractSlashCommand;
import niwer.photon.util.TranslationManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

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

        try (Statement statement = PhotonEngine.DATA_BASE.sqlConnection().createStatement()) {
            final boolean hasResultSet = statement.execute(command);

            if (!hasResultSet) {
                final int updateCount = statement.getUpdateCount();
                if (updateCount >= 0) {
                    return "Query executed successfully. Affected rows: " + updateCount;
                }
                return "Query executed successfully.";
            }

            try (ResultSet result = statement.getResultSet()) {
                final int columnCount = result.getMetaData().getColumnCount();
                final int[] columnWidths = new int[columnCount]; // E.G : [5, 10, 3] for 3 columns
                final StringBuilder builder = new StringBuilder();
                final List<String[]> rows = new ArrayList<>(); // List of rows, each row is an array of strings (The array represents the fields for each column)
                
                /* Calculate column size and save create rows with values for each column */
                for (int columnID = 0; columnID < columnCount; columnID++) {
                    String columnName = result.getMetaData().getColumnName(columnID + 1);
                    if (columnName == null) columnName = "";
                    if (columnName.length() > columnWidths[columnID]) columnWidths[columnID] = columnName.length();
                }
                while (result.next()) {
                    String[] row = new String[columnCount];
                    for (int columnID = 0; columnID < columnCount; columnID++) {
                        String field = result.getString(columnID + 1);
                        if (field == null) field = ""; // If null, set empty string
                        
                        row[columnID] = field; // Save field in the row
                        if (field.length() > columnWidths[columnID]) columnWidths[columnID] = field.length(); // If the field is wider than the current width, set it
                    }
                    rows.add(row);
                }
                
                /* Create the board (if markdown is set to true, then we'll print with markdown formatting) */
                if (markdown) builder.append("```\n");
                /* Title row */
                builder.append("| ");
                for (int i = 0; i < columnCount; i++) {
                    String columnName = result.getMetaData().getColumnName(i + 1);
                    if (columnName == null) columnName = "";
                    builder.append(String.format("%-" + columnWidths[i] + "s", columnName));
                    builder.append(" | ");
                }
                builder.append("\n|");
                for (int i = 0; i < columnCount; i++) builder.append(" ").append("-".repeat(columnWidths[i])).append(" |");
                builder.append("\n");
                
                /* Data rows */
                for (final String[] row : rows) {
                    builder.append("| ");
                    for (int i = 0; i < columnCount; i++) {
                        final String value = row[i];
                        builder.append(String.format("%-" + columnWidths[i] + "s", value)).append(" | ");
                    }
                    builder.append("\n");
                }
                if (markdown) builder.append("```");

                return new String(builder.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            }
        } finally {
            PhotonEngine.DATA_BASE.disconnect();
        }
    }
}

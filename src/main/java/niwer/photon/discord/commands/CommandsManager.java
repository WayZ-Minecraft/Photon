package niwer.photon.discord.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import niwer.photon.discord.commands.network.ExecuteSQLCommand;
import niwer.photon.discord.commands.network.PostUpdateCommand;
import niwer.photon.discord.commands.network.RestartNetworkCommand;
import niwer.photon.discord.commands.network.accounts.ListAccountsCommand;
import niwer.photon.discord.commands.network.accounts.ManagerAccountCommand;

@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class CommandsManager extends ListenerAdapter {
    public static final Map<String, AbstractSlashCommand> COMMANDS = new HashMap<>(); // Commands that are also available in private messages
    
    /**
     * Load slash commands, to be registered in discord slash commands interface
     * 
     * @author Mini
     */
    public static void load() {
        /* Private message and Guild commands */
        new LinkAccountCommand().register();
        new LanguageCommand().register();

        /* Guild only commands */
        new ClearCommand().register();
        {
            /* Official Server commands */
            new RestartNetworkCommand().register();
            new PostUpdateCommand().register();

            new ListAccountsCommand().register();
            new ManagerAccountCommand().register();

            new ExecuteSQLCommand().register();
        }

        {
            /* User servers commands */
            //TODO
        }
    }

    @Nonnull
    public static List<CommandData> getGlobalCommands() {
        return COMMANDS.entrySet().stream().filter(entry -> entry.getValue().isGlobal()).map(entry -> entry.getValue().DATA).toList();
    }

    @Nonnull
    public static List<CommandData> getGuildCommands() {
        return COMMANDS.entrySet().stream().filter(entry -> !entry.getValue().isGlobal()).map(entry -> entry.getValue().DATA).toList();
    }

    /**
     * Handle slash commands
     * 
     * @param event The event that triggered this command
     * @author Mini
     */
    public static void onSlashCommand(SlashCommandInteractionEvent event) {
        final String name = event.getName();
        final AbstractSlashCommand command = COMMANDS.get(name);
        if(command != null) {
            command.handle(event);
            return;
        }
    }

    /**
     * Handle auto complete interaction
     * Automatically finds and uses the registered provider for the command/option
     * Completely automatic - uses AutoCompleteRegistry to find providers.
     * No hardcoded command names or options!
     * 
     * @param event The event that triggered this command
     */
    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        String commandName = event.getName();
        String optionName = event.getFocusedOption().getName();
        String userInput = event.getFocusedOption().getValue();
        
        // Try to find a registered provider
        AutoCompleteRegistry.AutoCompleteProvider provider = AutoCompleteRegistry.getProvider(commandName, optionName);
        
        if (provider != null) {
            List<Command.Choice> choices = provider.provide(userInput);
            event.replyChoices(choices).queue();
        } else {
            // No provider registered - reply with empty list
            event.replyChoices().queue();
        }
    }
}
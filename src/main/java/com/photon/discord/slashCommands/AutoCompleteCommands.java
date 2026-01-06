package com.photon.discord.slashCommands;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

/**
 * Handle auto-completion for slash commands.
 * Completely automatic - uses AutoCompleteRegistry to find providers.
 * No hardcoded command names or options!
 * 
 * @author noz43
 * @version 2.0
 */
public class AutoCompleteCommands extends ListenerAdapter {

    /**
     * Handle auto complete interaction
     * Automatically finds and uses the registered provider for the command/option
     * 
     * @param event The event that triggered this command
     */
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
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
package com.photon.discord.slashCommands;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

public class AutoCompleteCommands extends ListenerAdapter{
    /* Class to manage commande auto-completion */
    private static String[] keysTime = new String[]{"10 minutes", "30 minutes", "1 hour", "6 hour", "1 day", "1 week", "1 month"};
    private static String[] keysFile = new String[]{"mod", "launcher", "api", "network"};
    

    /**
     * Handle auto complete interaction
     * @param event The event that triggered this command
     */
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        
        // Tempmute command
        if (event.getName().equals("tempmute") && event.getFocusedOption().getName().equals("duration")) {
            List<Command.Choice> options = Stream.of(keysTime)
                .filter(key -> key.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                .map(key -> new Command.Choice(key, key))
                .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }

        // Network File Update command
        else if (event.getName().equals("post-update") && event.getFocusedOption().getName().equals("filetype")) {
            List<Command.Choice> options = Stream.of(keysFile)
                .filter(key -> key.startsWith(event.getFocusedOption().getValue()))
                .map(key -> new Command.Choice(key, key))
                .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }
}

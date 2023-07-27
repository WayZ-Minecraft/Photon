package com.photon.discord.slashCommands;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

public class AutoCompleteCommands extends ListenerAdapter{
    /* Class to manage commande auto-completion */
    private static HashMap<String, Integer> muteDuration = new HashMap<String, Integer>(){{
        put("10 minutes", 10);
        put("30 minutes", 30);
        put("1 hour", 60);
        put("6 hour", 360);
        put("1 day", 1440);
        put("1 week", 10080);
        put("1 month", 43200);
    }};

    private static String[] keys = muteDuration.keySet().toArray(new String[0]);
    

    /**
     * Handle auto complete interaction
     * @param event The event that triggered this command
     */
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        System.out.println(muteDuration);
        if (event.getName().equals("tempmute") && event.getFocusedOption().getName().equals("duration")) {
            List<Command.Choice> options = Stream.of(keys)
                .filter(key -> key.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                .map(key -> new Command.Choice(key, muteDuration.get(key)))
                .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }
}

package com.photon.discord.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.photon.util.NetworkOnly;

import net.dv8tion.jda.api.interactions.commands.Command;

/**
 * Registry system for command autocomplete providers.
 * Commands can register their autocomplete sources here instead of hardcoding in AutoCompleteCommands.
 * 
 * @author noz43
 * @version 1.0
 */
@NetworkOnly
public class AutoCompleteRegistry {
    
    /**
     * Functional interface for autocomplete providers
     */
    @FunctionalInterface
    public interface AutoCompleteProvider {
        /**
         * Provide autocomplete choices based on user input
         * @param userInput The current user input for filtering
         * @return List of Command.Choice for autocomplete
         */
        List<Command.Choice> provide(String userInput);
    }
    
    // Registry: commandName -> optionName -> provider
    private static final Map<String, Map<String, AutoCompleteProvider>> registry = new HashMap<>();
    
    /**
     * Register an autocomplete provider for a specific command and option
     * 
     * @param commandName The name of the command (e.g. "tempmute")
     * @param optionName The name of the option (e.g. "duration")
     * @param provider The autocomplete provider function
     */
    public static void register(String commandName, String optionName, AutoCompleteProvider provider) {
        registry.computeIfAbsent(commandName, k -> new HashMap<>()).put(optionName, provider);
    }
    
    /**
     * Register an autocomplete provider using a collection of choices
     * 
     * @param commandName The name of the command
     * @param optionName The name of the option
     * @param choicesProvider Function that provides available choices
     */
    public static void registerFromCollection(String commandName, String optionName, Function<Void, Iterable<String>> choicesProvider) {
        register(commandName, optionName, userInput -> {
            Iterable<String> choices = choicesProvider.apply(null);
            return streamFrom(choices)
                .filter(choice -> choice.toLowerCase().startsWith(userInput.toLowerCase()))
                .map(choice -> new Command.Choice(choice, choice))
                .collect(Collectors.toList());
        });
    }
    
    /**
     * Register an autocomplete provider using an enum
     * 
     * @param commandName The name of the command
     * @param optionName The name of the option
     * @param enumClass The enum class providing choices
     */
    public static void registerFromEnum(String commandName, String optionName, Class<? extends Enum<?>> enumClass) {
        register(commandName, optionName, userInput -> {
            Enum<?>[] values = enumClass.getEnumConstants();
            List<Command.Choice> choices = new java.util.ArrayList<>();
            for (Enum<?> value : values) {
                String name = value.name().toLowerCase();
                if (name.startsWith(userInput.toLowerCase())) {
                    choices.add(new Command.Choice(name, name));
                }
            }
            return choices;
        });
    }
    
    /**
     * Get the autocomplete provider for a specific command and option
     * 
     * @param commandName The command name
     * @param optionName The option name
     * @return The provider, or null if not registered
     */
    public static AutoCompleteProvider getProvider(String commandName, String optionName) {
        Map<String, AutoCompleteProvider> commandProviders = registry.get(commandName);
        if (commandProviders == null) return null;
        return commandProviders.get(optionName);
    }
    
    /**
     * Check if a command/option combination has a registered provider
     * 
     * @param commandName The command name
     * @param optionName The option name
     * @return true if registered, false otherwise
     */
    public static boolean hasProvider(String commandName, String optionName) {
        return getProvider(commandName, optionName) != null;
    }
    
    /**
     * Clear all registered providers (useful for testing)
     */
    public static void clear() {
        registry.clear();
    }
    
    // Helper method to convert Iterable to Stream
    private static java.util.stream.Stream<String> streamFrom(Iterable<String> iterable) {
        return java.util.stream.StreamSupport.stream(iterable.spliterator(), false);
    }
}
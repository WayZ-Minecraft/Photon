package com.photon.discord.commands;

import java.util.function.Function;

import com.photon.discord.commands.AutoCompleteRegistry.AutoCompleteProvider;
import com.photon.util.NetworkOnly;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * @author Niwer
 */
@NetworkOnly
public abstract class AbstractSlashCommand {

    private final String cmdName;
    protected final CommandData data;

    protected AbstractSlashCommand(String cmdName, String cmdDescription, String group) {
        this.cmdName = cmdName;
        this.data = Commands.slash(this.cmdName, cmdDescription)/* .addSubcommandGroups(null) */;
    }

    protected AbstractSlashCommand(String cmdName, String cmdDescription) {
        this.cmdName = cmdName;
        this.data = Commands.slash(this.cmdName, cmdDescription);
    }

    public SlashCommandData data() { return (SlashCommandData) this.data; }

    /**
     * Add an option without autocomplete support
     * @param type The option type
     * @param name The option name
     * @param description The option description
     * @param isRequired Whether the option is required
     * @return The SlashCommandData for chaining
     */
    protected SlashCommandData addOption(OptionType type, String name, String description, boolean isRequired) {
        return this.addOption(type, name, description, isRequired, false);
    }

    /**
     * Add an option with autocomplete support
     * @param type The option type
     * @param name The option name
     * @param description The option description
     * @param isRequired Whether the option is required
     * @param autoComplete The autocomplete provider
     * @return The SlashCommandData for chaining
     */
    protected SlashCommandData addOption(OptionType type, String name, String description, boolean isRequired, AutoCompleteProvider autoComplete) {
        final SlashCommandData cmd = this.addOption(type, name, description, isRequired, true);
        if(autoComplete != null) AutoCompleteRegistry.register(this.cmdName, name, autoComplete);
        return cmd;
    }
    
    /**
     * Add an option with autocomplete support
     * @param type The option type
     * @param name The option name
     * @param description The option description
     * @param isRequired Whether the option is required
     * @param autoCompleteEnum The autocomplete enum provider
     * @return The SlashCommandData for chaining
     */
    protected SlashCommandData addOption(OptionType type, String name, String description, boolean isRequired, Class<? extends Enum<?>> autoCompleteEnum) {
        final SlashCommandData cmd = this.addOption(type, name, description, isRequired, true);
        if(autoCompleteEnum != null) AutoCompleteRegistry.registerFromEnum(this.cmdName, name, autoCompleteEnum);
        return cmd;
    }

    /**
     * Add an option with autocomplete support
     * @param type The option type
     * @param name The option name
     * @param description The option description
     * @param isRequired Whether the option is required
     * @param choicesProvider The autocomplete choices provider
     * @return The SlashCommandData for chaining
     */
    protected SlashCommandData addOption(OptionType type, String name, String description, boolean isRequired, Function<Void, Iterable<String>> choicesProvider) {
        final SlashCommandData cmd = this.addOption(type, name, description, isRequired, true);
        if(choicesProvider != null) AutoCompleteRegistry.registerFromCollection(this.cmdName, name, choicesProvider);
        return cmd;
    }

    private SlashCommandData addOption(OptionType type, String name, String description, boolean isRequired, boolean autoComplete) {
        return this.data().addOption(type, name, description, isRequired, autoComplete);
    }

    public void register() {
        CommandsManager.COMMANDS.put(this.cmdName, this);
    }

    /**
     * Is this command global (available in private messages) ?
     * @return true if global, false if guild only
     */
    public boolean isGlobal() { return false; }

    public abstract void handle(SlashCommandInteractionEvent event);
}

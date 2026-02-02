package com.photon.discord.commands;

import com.photon.util.NetworkOnly;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

    protected AbstractSlashCommand(String cmdName, String cmdDescription) {
        this.cmdName = cmdName;
        this.data = Commands.slash(this.cmdName, cmdDescription);
    }

    public SlashCommandData data() {
        return (SlashCommandData) this.data;
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

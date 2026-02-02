package com.photon.discord.commands.network;

import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.NetworkEngine;
import com.photon.util.os.ApplicationUtils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public class RestartNetworkCommand extends AbstractSlashCommand {

    public RestartNetworkCommand() {
        super("restart-network", "Restarts the network.");
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    /**
     * Clear a number of messages in the channel (/clear number)
     * 
     * @param event The event that triggered this command
     * @author Mini
     */
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.reply("Restarting network...").queue();
        ApplicationUtils.restart(NetworkEngine.class, "--restart");
    }
}
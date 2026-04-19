package niwer.photon.discord.commands.network;

import niwer.photon.PhotonEngine;
import niwer.photon.discord.commands.AbstractSlashCommand;
import niwer.photon.util.TranslationManager;
import niwer.photon.util.os.ApplicationUtils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
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
        if (!isOfficialGuild(event)) return; // Check if we're on the official guild
        if (!isConsoleChannel(event)) return; // Check if we're in the console channel

        event.reply(TranslationManager.format(event.getUser().getId(), "command.reply.restart_network.success")).setEphemeral(true).queue();
        ApplicationUtils.restart(PhotonEngine.class, "--restart");
    }
}
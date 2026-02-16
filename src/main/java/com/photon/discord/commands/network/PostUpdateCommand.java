package com.photon.discord.commands.network;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.photon.discord.BotEngine;
import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.NetworkDirectories;
import com.photon.network.NetworkEngine;
import com.photon.util.os.ApplicationUtils;
import com.photon.util.updater.UpdateChannel;
import com.photon.util.updater.UpdateFileType;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class PostUpdateCommand extends AbstractSlashCommand {

    public PostUpdateCommand() {
        super("post-update", "Posts an update on the network.");
        this.addOption(OptionType.ATTACHMENT, "file", "The build file to post", true);
        this.addOption(OptionType.STRING, "file_type", "The file to update (e.g: mod, launcher)", true, UpdateFileType.class);
        this.addOption(OptionType.STRING, "channel", "The channel (e.g: stable, dev or test)", false, UpdateChannel.class);
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
        /* If we're not on the official guild */
        if(!BotEngine.isOfficialGuild(event.getGuild())) {
            event.reply("You're not on the official guild.").setEphemeral(true).queue();
            return;
        }

        final Attachment FILE = event.getOption("file").getAsAttachment(); // Should always be present
        if(!FILE.getFileExtension().contains(".jar")) {
            event.reply("You've submitted an invalid file type. Please submit a JAR file.").setEphemeral(true).queue();
            return; // If the update isn't a JAR file, then skip it (for security reasons)
        }

        final UpdateFileType FILE_TYPE = UpdateFileType.fromString(event.getOption("file_type").getAsString()); // Should always be present (MOD, LAUNCHER, etc)
        final OptionMapping CHANNEL_ARG = event.getOption("channel"); // May be null, so wedefault to STABLE
        final UpdateChannel CHANNEL = CHANNEL_ARG != null ? UpdateChannel.fromString(CHANNEL_ARG.getAsString()) : UpdateChannel.STABLE;

        /* Data to download the file */
        final Path OUTPUT_PATH = Path.of(NetworkDirectories.getPathForUpdateChannel(FILE_TYPE, CHANNEL));

        /* Try upload the file */
        try (InputStream stream = new URL(FILE.getUrl()).openStream()) {
            /* Copy the uploaded file to the output path */
            try {
                /* Try to create the parent directories, if they don't exist */
                OUTPUT_PATH.getParent().toFile().mkdirs();

                /* Copy the file */
                Files.copy(stream, OUTPUT_PATH, StandardCopyOption.REPLACE_EXISTING);
                
                /* If the updated file is the network, restart the network engine */
                if (FILE_TYPE == UpdateFileType.NETWORK) {
                    event.reply("You've uploaded the network update. Restarting the network engine...").queue();
                    ApplicationUtils.restart(NetworkEngine.class, "--restart");
                } else event.reply(String.format("The update for **%s** (Channel: **%s**) has been posted. (**%s**)", FILE_TYPE, CHANNEL, OUTPUT_PATH)).queue();
            } catch (NoSuchFileException e) {
                event.reply("The file path in the Network config is invalid: " + OUTPUT_PATH).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            event.reply("Error while uploading update file :" + e).setEphemeral(true).queue();
        }
    }
}
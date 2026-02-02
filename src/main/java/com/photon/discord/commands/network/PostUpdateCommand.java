package com.photon.discord.commands.network;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class PostUpdateCommand extends AbstractSlashCommand {

    public PostUpdateCommand() {
        super("post-update", "Posts an update on the network.");
        this.data().addOption(OptionType.ATTACHMENT, "file", "The build file to post", true, false);
        this.data().addOption(OptionType.STRING, "file_type", "The file to update (e.g: mod, launcher)", true, true);
        this.data().addOption(OptionType.STRING, "channel", "The channel (e.g: stable, dev or test)", false, true);
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
        final Attachment file = event.getOption("file").getAsAttachment();
        final String[] fileName = file.getFileName().split("\\.|\\-");
        final String fileTypeString = event.getOption("type") == null ? fileName[0]
                : event.getOption("type").getAsString();
        final String channelTypeString = event.getOption("channel") == null
                ? fileName.length >= 3 ? fileName[1] : "stable"
                : event.getOption("channel").getAsString();

        /* Data to download the file */
        final UpdateFileType fileType = UpdateFileType.valueOf(fileTypeString.toUpperCase());
        final UpdateChannel channelType = UpdateChannel.valueOf(channelTypeString.toUpperCase());
        final Path outputPath = Path.of(NetworkDirectories.getPathForUpdateChannel(fileType, channelType));

        InputStream inputStream;
        try {
            inputStream = new URL(file.getUrl()).openStream();
            try {
                Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (NoSuchFileException e) {
                File fileOutput = new File(outputPath.toString()).getParentFile();
                fileOutput.mkdirs();
                Files.copy(inputStream, outputPath);
            } finally {
                inputStream.close();
                event.reply("File updated into : " + outputPath).queue();
                if (UpdateFileType.LAUNCHER == fileType)
                    ApplicationUtils.restart(NetworkEngine.class, "--restart");
            }
        } catch (Exception e) {
            event.reply("Error with file :" + e).queue();
        }
    }
}
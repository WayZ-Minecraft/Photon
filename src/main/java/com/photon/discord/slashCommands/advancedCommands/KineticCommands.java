package com.photon.discord.slashCommands.advancedCommands;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import com.photon.util.ProtectorManager;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class KineticCommands {

    /**
     * This allow a converting files from NEBULAE (+varients) to orignial files and the revert
     * @param event The event that triggered a SlashCommandInteractionEvent
     */
    public static void covnertFile(SlashCommandInteractionEvent event) {
        final Attachment file = event.getOption("file").getAsAttachment();
        FileUpload upload = null;
        switch(file.getFileExtension()) {
        case "obj":
            upload = compress("nebulae", file);
            break;
        case "png":
            upload = compress("nebulae-image", file);
            break;
        case "anim":
            upload = compress("nebulae-anim", file);
            break;

        case "nebulae":
            upload = decompress("obj", file);
            break;
        case "nebulae-image":
            upload = decompress("png", file);
            break;
        case "nebulae-anim":
            upload = decompress("anim", file);
            break;
        }
        if(upload !=null) event.replyFiles(upload).queue();
        else event.reply("Failed to convert file").queue();
    }

    private static FileUpload decompress(String extention, Attachment attach) {
        final String name = attach.getFileName();
        try {
            InputStream inputStream = new URL(attach.getUrl()).openStream();
            final byte[] bytes = ProtectorManager.readCompressedFile(inputStream);

            final FileUpload file = FileUpload.fromData(bytes, name.substring(0, name.indexOf(".")+1)+extention);
            inputStream.close();
            return file;
        } catch (Exception e) {}
        return null;
    }

    private static FileUpload compress(String extention, Attachment attach) {
        final String name = attach.getFileName();
        try {
            InputStream inputStream = new URL(attach.getUrl()).openStream();

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ProtectorManager.writeCompressedFile(out, inputStream.readAllBytes());

            final FileUpload file = FileUpload.fromData(out.toByteArray(), name.substring(0, name.indexOf(".")+1)+extention);
            inputStream.close();
            return file;
        } catch (Exception e) {}
        return null;
    }
}

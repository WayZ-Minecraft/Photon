package com.photon.discord.slashCommands.advancedCommands;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import com.photon.util.ProtectorManager;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;

public class KineticCommands {

    public static void opacityImage(SlashCommandInteractionEvent event) throws IOException {
        /* Get attachments */
        Attachment textureAttachment = event.getOption("texture").getAsAttachment();
        Attachment opacityMapAttachment = event.getOption("opacity_map").getAsAttachment();
        OptionMapping customFormatOption = event.getOption("custom_format");
        OptionMapping reversedColorsOption = event.getOption("reversed_colors");
        boolean customFormat = customFormatOption !=null ? customFormatOption.getAsBoolean() : false;
        boolean reversedColors = reversedColorsOption !=null ? reversedColorsOption.getAsBoolean() : false;
        
        /* Read images */
        InputStream textureStream = new URL(textureAttachment.getUrl()).openStream();
        BufferedImage texture = ImageIO.read(textureStream);
        InputStream opacityMapStream = new URL(opacityMapAttachment.getUrl()).openStream();
        BufferedImage opacityMap = ImageIO.read(opacityMapStream);
        
        /* Creating the result */
        BufferedImage result = new BufferedImage(texture.getWidth(), texture.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int x = 0; x < result.getWidth(); x++) {
            for(int y = 0; y < result.getHeight(); y++) {
                /* Get colors */
                Color overlayPxColor = new Color(opacityMap.getRGB(x, y));
                Color pxColor = new Color(texture.getRGB(x, y));
                int rgb = pxColor.getRGB();
                
                /* Calculate the new color */
                if(reversedColors ? overlayPxColor.equals(Color.white) : overlayPxColor.equals(Color.black))
                    rgb = new Color(0, 0, 0, 0).getRGB(); /* Color is fully equals to the full WHITE or BLACK, we set 0 opacity */
                if(checkColor(overlayPxColor))
                    rgb = new Color(overlayPxColor.getRed(), overlayPxColor.getGreen(), overlayPxColor.getBlue(), pxColor.getRed()).getRGB();
                
                /* Set the new color */
                result.setRGB(x, y, rgb);
            }
        }

        /* Creating the result */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String fileName = textureAttachment.getFileName().replace("."+textureAttachment.getFileExtension(), "");
        ImageIO.write(result, "PNG", out);
        FileUpload file = FileUpload.fromData(out.toByteArray(), fileName+"_opacity_map"+(customFormat ? ".nebulae-image" : ".png"));

        /* Closing resources */
        textureStream.close();
        opacityMapStream.close();
        out.close();

        /* Sending result */
        event.replyFiles(file).queue();
    }

    /**
     * Check if the color is between 0 and 255
     * @param c The color to check
     * @return true if the color is between 0 and 255
     */
    private static boolean checkColor(Color c) {
        return c.getRed() > 0 && c.getRed() < 255 && c.getGreen() > 0 && c.getGreen() < 255 && c.getBlue() > 0 && c.getBlue() < 255;
    }

    /**
     * This allow a converting files from NEBULAE (+varients) to orignial files and the revert
     * @param event The event that triggered a SlashCommandInteractionEvent
     */
    public static void convertFile(SlashCommandInteractionEvent event) {
        Attachment file = event.getOption("file").getAsAttachment();
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

    public static void imageQuality(SlashCommandInteractionEvent event) throws IOException {
        Attachment file = event.getOption("image").getAsAttachment();
        int qualityPercentage = event.getOption("percentage").getAsInt();
        if(qualityPercentage < 0 || qualityPercentage > 100) {
            event.reply("The quality percentage must be between 0 and 100").queue();
            return;
        }
        try {
            /* Open stream and read image */
            InputStream inputStream = new URL(file.getUrl()).openStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedImage image = ImageIO.read(inputStream);
            
            /* Create the new file */
            ImageWriter writer = ImageIO.getImageWritersByFormatName("PNG").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(qualityPercentage / 100f);
            writer.setOutput(out);
            writer.write(null, new IIOImage(image, null, null), param);

            /* Reply with the new file */
            FileUpload upload = FileUpload.fromData(out.toByteArray(), file.getFileName());
            event.replyFiles(upload).queue();
            
            /* Close streams */
            inputStream.close();
            out.close();
        } catch (Exception e) {
            event.reply("Failed to compress image").queue();
        }
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

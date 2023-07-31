package com.photon.discord.usersInteraction;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.photon.discord.usersInteraction.data.UsersInfo;
import com.photon.network.NetworkDirectories;
import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.progressbar.ColoredProgressbar;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class xpManager {

    /**
     * Calculate the xp value of a message
     * @param message The message to calculate the xp value
     * @return int : The xp value of the message
     */
    private static int xpValue(String message){
        final double x = Math.log(message.length()) / Math.log(5);
        return (int) Math.floor(x) + 1;
    }


    /**
     * When a user send a message, add the xp to his profile
     * @param event The event of a user sending a message
     */
    public static void onMessageReceived(MessageReceivedEvent event){
        final User user = event.getAuthor();
        final String message = event.getMessage().getContentDisplay();
        UsersInfo.addXp(user.getId(), xpValue(message));
    }
    
    public static void levelEmbed(SlashCommandInteractionEvent event){
        User user = event.getOption("user").getAsUser() == null ? event.getUser() : event.getOption("user").getAsUser();

        BufferedImage image = new BufferedImage(500, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        drawAvatar(g2d,user.getAvatar().getUrl(), 10, 10, 128, 128);

        
        int Userxp = UsersInfo.getXp(user.getId());
        

        
        PhotonInterfaceUtils.drawTextAlignedRight(g2d, String.format("%s/100", Userxp), 490 , 50, Color.LIGHT_GRAY, new Font("Arial", 0, 25));
        PhotonInterfaceUtils.drawText(g2d, "Level 1", 200, 50, Color.LIGHT_GRAY, new Font("Arial", 0, 25));

        JPanel panel = getProgressBar(Userxp, 100);
        panel.paint(g2d);
        g2d.dispose(); // Release resources


        // Save the BufferedImage as a PNG file
        try {
            File output = new File(NetworkDirectories.discordDirectory,"xpScreen.png");
            ImageIO.write(image, "png", output);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(user.getGlobalName() + "'s xp");
            embed.setImage("attachment://xpScreen.png");
            embed.setColor(Color.GREEN);
            FileUpload file = FileUpload.fromData(output);
            event.replyFiles(file).addEmbeds(embed.build()).queue();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void drawAvatar(Graphics2D g2d, String url, int x, int y, int width, int height) {
        try {
            BufferedImage avatar = ImageIO.read(new URL(url));
            g2d.drawImage(avatar, x, y, width, height, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JPanel getProgressBar(int Userxp, int XpLevel){
        JPanel panel = new JPanel();
        ColoredProgressbar progressBar = new ColoredProgressbar(Color.WHITE, Color.GREEN);
        progressBar.setBounds(200, 75, 300, 50);
        progressBar.setValue(Userxp);
        progressBar.setMaximum(XpLevel);
        progressBar.setArcSize(10, 10);

        panel.add(progressBar);
        panel.setSize(500, 150);
        panel.setBackground(new Color(0, true));

        return panel;
    }
}

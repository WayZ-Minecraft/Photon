package com.photon.discord.usersInteraction;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.photon.discord.usersInteraction.data.UsersInfo;
import com.photon.network.NetworkDirectories;
import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.progressbar.ColoredProgressbar;
import com.photon.ui.images.RoundedImage;

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
        final int widthPicture = 500;
        final int heightPicture = 160;

        User user = event.getOption("user") == null ? event.getUser() : event.getOption("user").getAsUser();

        BufferedImage image = new BufferedImage(widthPicture, heightPicture, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        PhotonInterfaceUtils.drawRoundedRect(g2d, 0, 0, widthPicture, heightPicture, 40, 40, new Color(24, 26, 28));

        drawAvatar(g2d,user.getAvatar().getUrl(), 10, 60, 80, 80);

        int Userxp = UsersInfo.getXp(user.getId());
              
        JPanel pb = getProgressBar(Userxp, 100);
        pb.paint(g2d);

        JPanel title = drawTitle();
        title.paint(g2d);

        g2d.dispose(); // Release resources
        
        
        // Save the BufferedImage as a PNG file
        try {
            File output = new File(NetworkDirectories.discordDirectory,"xpScreen.png");
            ImageIO.write(image, "png", output);

            FileUpload file = FileUpload.fromData(output);
            event.replyFiles(file).queue();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static JPanel drawTitle(){
        JPanel panel = new JPanel();
        RoundedImage roundedImage = new RoundedImage("src/main/resources/logo.png", 30, 30);
        roundedImage.setBounds(10, 10, 40, 40);


        String nameString = "<html><p style='color: #A4A4A4'>NIVEAU DE <span style='color: #8b2628'>Mini</span></p></html>";
        JLabel titleText = new JLabel(nameString);
        titleText.setBounds(60, 15, 300, 30);
        titleText.setFont(new Font("Arial", 0, 20));

        String rankString = "<html><p style='color: white'>#1</p></html>";
        JLabel rankText = new JLabel(rankString);
        rankText.setBounds(310, 15, 150, 30);
        rankText.setFont(new Font("Arial", 0, 20));
        rankText.setHorizontalAlignment(SwingConstants.RIGHT);


        panel.add(titleText);
        panel.add(roundedImage);
        panel.add(rankText);

        panel.setSize(500, 50);
        panel.setBackground(new Color(0, true));
        return panel;
    }

    private static void drawAvatar(Graphics2D g2d, String url, int x, int y, int width, int height) {
        try {
            BufferedImage avatar = ImageIO.read(new URL(url));
            PhotonInterfaceUtils.drawRoundedImage(g2d, avatar, x, y, width, height, 200, 200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JPanel getProgressBar(int Userxp, int XpLevel){
        final int decalx = 120;
        final int decaly = 80;
        final int boxWidth = 340;
        final int textSize = 18;

        JPanel panel = new JPanel();
        
        ColoredProgressbar progressBar = new ColoredProgressbar(new Color(100, 100, 100), new Color(139, 38, 40));
        progressBar.setBounds(0 + decalx, 20 + textSize + decaly, boxWidth, 12);
        progressBar.setValue(Userxp);
        progressBar.setMaximum(XpLevel);
        progressBar.setArcSize(10, 10);
        
        String levelString = "<html><p style='color: white'>NIVEAU <span style='color: #8b2628'>1</span></p></html>";
        JLabel level = new JLabel(levelString);
        level.setBounds(0 + decalx, 0 + decaly, boxWidth/2, 30);
        level.setFont(new Font("Arial", 0, textSize));
        
        String xpString = String.format("<html><p style='color: #A4A4A4'>%s <span style='color: white'>/</span> <span style='color: #8b2628'>%s</span></p></html>", Userxp, XpLevel);
        JLabel xp = new JLabel(xpString);
        xp.setBounds(decalx + boxWidth/2, decaly, boxWidth/2, 30);
        xp.setHorizontalAlignment(SwingConstants.RIGHT);
        xp.setFont(new Font("Arial", 0, textSize));


        
        panel.add(progressBar);
        panel.add(level);
        panel.add(xp);

        panel.setBackground(new Color(0, true));
        panel.setSize(500, 150);

        return panel;
    }
}

package com.photon.discord.usersInteraction;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.photon.discord.usersInteraction.data.UsersInfo;
import com.photon.network.NetworkDirectories;
import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.progressbar.ColoredProgressbar;
import com.photon.util.ConsoleManager;
import com.photon.util.TranslationManager;
import com.photon.util.ConsoleManager.EnumLogType;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class XpManager {

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
        try {
            UsersInfo.addXp(user.getId(), xpValue(message));
        } catch (SQLException ignor) {
            ConsoleManager.create("An error occured while adding xp to " + user.getGlobalName()+"Please check").error().withType(EnumLogType.NETWORK).displayOnDiscord().end();
        }
    }

    /**
     * Give xp to a user
     * @param event The event of a SlashCommand
     */
    public static void giveXp(SlashCommandInteractionEvent event){
        final User user = event.getOption("user").getAsUser();
        final int xp = event.getOption("xp").getAsInt();
        try {
            UsersInfo.addXp(user.getId(), xp);
        } catch (SQLException e) {
            event.reply("An error occured while giving xp to " + user.getGlobalName()).queue();
        }
        event.reply("You have give " + xp + " xp to" + user.getGlobalName()).queue();
    }

    /**re
     * Remove xp to a user
     * @param event The event of a SlashCommand
     */
    public static void removeXp(SlashCommandInteractionEvent event){
        final User user = event.getOption("user").getAsUser();
        final int xp = event.getOption("xp").getAsInt();
        try {
            UsersInfo.removeXp(user.getId(), xp);
        } catch (SQLException e) {
            event.reply("An error occured while removing xp to " + user.getGlobalName()).queue();
        }
        event.reply("You have remove " + xp + " xp to " + user.getGlobalName()).queue();
    }
    
    /**
     * Create the picture of the level of a user, and reply it to the user
     * @param event The event of a SlashCommand
     */
    public static void levelEmbed(SlashCommandInteractionEvent event){
        final int widthPicture = 500;
        final int heightPicture = 160;
        
        try {
            final User user = event.getOption("user") == null ? event.getUser() : event.getOption("user").getAsUser();
            final String userName = user.getGlobalName();
            final int userRank = UsersInfo.getRank(user.getId());
            final int Userxp = UsersInfo.getXp(user.getId());
            final int userLevel = UsersInfo.getLevel(user.getId());
            final int userXpToNextLevel = UsersInfo.getXpToNextLevel(user.getId());

            BufferedImage image = new BufferedImage(widthPicture, heightPicture, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            PhotonInterfaceUtils.drawRoundedRect(g2d, 0, 0, widthPicture, heightPicture, 40, 40, new Color(24, 26, 28));

            drawAvatar(g2d,user.getAvatar().getUrl(), 10, 60, 80, 80);

                
            JPanel pb = getProgressBar(Userxp, userXpToNextLevel, userLevel);
            pb.paint(g2d);

            JPanel title = drawTitle(userName, userRank);
            title.paint(g2d);

            g2d.dispose(); // Release resources
        
        
            // Save the BufferedImage as a PNG file
            File output = new File(NetworkDirectories.discordDirectory,"xpScreen.png");
            ImageIO.write(image, "png", output);

            FileUpload file = FileUpload.fromData(output);
            event.replyFiles(file).queue();

        } catch (IOException e) {
            e.printStackTrace();
            event.reply("An error occured while getting the level of " + event.getUser().getGlobalName() + "Please retry later. (Staff is notified)").queue();
        } catch (SQLException e) {
            event.reply("An error occured while getting the level of " + event.getUser().getGlobalName() + "Please retry later. (Staff is notified)").queue();
        }


    }

    private static JPanel drawTitle(String name, int rank){
        JPanel panel = new JPanel();


        String nameString = "<html><p style='color: #A4A4A4'>"+TranslationManager.format("discord.levelMessage.title")+"<span style='color: #8b2628'>"+name+"</span></p></html>";
        JLabel titleText = new JLabel(nameString);
        titleText.setBounds(20, 15, 300, 30);
        titleText.setFont(new Font("Arial", 0, 20));

        String rankString = "<html><p style='color: white'>#"+rank+"</p></html>";
        JLabel rankText = new JLabel(rankString);
        rankText.setBounds(310, 15, 150, 30);
        rankText.setFont(new Font("Arial", 0, 20));
        rankText.setHorizontalAlignment(SwingConstants.RIGHT);


        panel.add(titleText);
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

    /**
     * Create a JPanel with the progress bar
     * @param Userxp The xp of the user
     * @param XpLevel The xp needed to level up
     * @param levelNumber The level of the user
     * @return JPanel : The JPanel with the progress bar
     */
    private static JPanel getProgressBar(int Userxp, int XpLevel, int levelNumber){
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
        
        String levelString = "<html><p style='color: white'>"+TranslationManager.format("discord.levelMessage.description")+"<span style='color: #8b2628'>"+levelNumber+"</span></p></html>";
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

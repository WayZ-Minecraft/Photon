package com.photon.discord.slashCommands.advancedCommands;

import java.sql.SQLException;
import java.util.Date;

import com.photon.discord.BotEngine;
import com.photon.discord.Channels;
import com.photon.network.objects.ObjectNews;
import com.photon.network.sql.SQLnews;
import com.photon.network.sql.SqlInteract;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class NewsManager {
    
    /**
     * Create a news, save it in the database and send it in the news channel
     * @param event the event of a slash command
     */
    public static void createNews(SlashCommandInteractionEvent event) {
        String title = event.getOption("title").getAsString();
        String contentEn = event.getOption("content-en").getAsString();
        String contentFr = event.getOption("content-fr").getAsString();
        Date date = new Date(System.currentTimeMillis());
        String imageUrl = event.getOption("image").getAsAttachment().getUrl();

        ObjectNews news = new ObjectNews(title, contentEn, contentFr, date, imageUrl);

        try{
            SQLnews.createNews(news);
        }catch (SQLException e) {
            ConsoleManager.create("Erreur on creating news : " + e.getMessage()).displayOnDiscord().withType(EnumLogType.NETWORK).error().end();
            SqlInteract.connect();
            event.reply("Error on news creation, please show chanel console-manager").queue();
        }

        BotEngine.guild.getTextChannelById(Channels.TEXT_BOT.id).sendMessageEmbeds(news.getEmbed().build()).queue();
        event.reply("News created").queue();
    }


}

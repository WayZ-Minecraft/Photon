package com.photon.discord.slashCommands.advancedCommands;

import java.sql.Date;
import java.sql.SQLException;

import com.photon.network.objects.ObjectNews;
import com.photon.network.sql.SQLnews;
import com.photon.network.sql.SqlInteract;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class NewsManager {
    
    public static void createNews(SlashCommandInteractionEvent event) {
        String title = event.getOption("title").getAsString();
        String content = event.getOption("content").getAsString();
        Date date = new Date(System.currentTimeMillis());
        String imageUrl = event.getOption("image").getAsAttachment().getUrl();

        ObjectNews news = new ObjectNews(title, content, date, imageUrl);

        try{
            SQLnews.createNews(news);
        }catch (SQLException e) {
            ConsoleManager.create("Erreur on creating news : " + e.getMessage()).displayOnDiscord().withType(EnumLogType.NETWORK).error().end();
            SqlInteract.connect();
            event.reply("Error on news creation, please show chanel console-manager").queue();
        }

        event.reply("News created").queue();
    }
}

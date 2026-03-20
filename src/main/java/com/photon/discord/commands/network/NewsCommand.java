package com.photon.discord.commands.network;

import java.sql.SQLException;
import java.util.Date;

import com.photon.PhotonEngine;
import com.photon.discord.commands.AbstractSlashCommand;
import com.photon.network.objects.ObjectNews;
import com.photon.sql.NewsTable;
import com.photon.util.NetworkOnly;
import com.photon.util.PhotonLogTypes;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import niwer.lumen.Console;

/**
 * @author Niwer
 */
@NetworkOnly
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class NewsCommand extends AbstractSlashCommand {

    public NewsCommand() {
        super("news", "Create a news to send to the news channel");
        this.addOption(OptionType.STRING, "title", "The title of the news", true);
        this.addOption(OptionType.STRING, "content-en", "The content of the news in English", true);
        this.addOption(OptionType.STRING, "content-fr", "The content of the news in French", true);
        this.addOption(OptionType.ATTACHMENT, "image", "The image of the news", true);
        this.data().setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    /**
     * Create a news, save it in the database and send it in the news channel
     * @param event the event of a slash command
     * //TODO
     */
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String title = event.getOption("title").getAsString();
        String contentEn = event.getOption("content-en").getAsString();
        String contentFr = event.getOption("content-fr").getAsString();
        Date date = new Date(System.currentTimeMillis());
        String imageUrl = event.getOption("image").getAsAttachment().getUrl();

        ObjectNews news = new ObjectNews(title, contentEn, contentFr, date, imageUrl);

        try{
            NewsTable.createNews(news);
        }catch (SQLException e) {
            Console.log("Unable to creating news : " + e.getMessage()).sendToProcessor().type(PhotonLogTypes.DISCORD_BOT).error().container(PhotonEngine.LOGGER).send();
            event.reply("Error on news creation, please show chanel console-manager").queue();
        }

        // BotEngine.guild.getTextChannelById(Channels.TEXT_BOT.id).sendMessageEmbeds(news.getEmbed().build()).queue();
        event.reply("News created").queue();
    }
}

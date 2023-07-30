package com.photon.discord.usersInteraction;

import com.photon.discord.usersInteraction.data.UsersInfo;
import com.photon.discord.usersInteraction.language.Languages;
import com.photon.util.ConsoleManager;
import com.photon.util.TranslationManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberJoin extends ListenerAdapter {

    /**
     * Send a welcome message to the new member
     * @param event event triggered when a new member join the server
     * @author Mini
     */
    public static void onMemberJoin(GuildMemberJoinEvent event) {
        event.getUser().openPrivateChannel().queue(channel -> {

            ConsoleManager.create("Welcome message sent to " + event.getUser().getName()).displayOnDiscord().end();

            EmbedBuilder embed = buildEmbed(UsersInfo.getLanguage(event.getUser().getId()).get(0));
            channel.sendMessageEmbeds(embed.build()).queue();
        });
    }

    /**
     * When a user get a role, send him a welcome message if it's his first connection
     * @param event The event of a user getting a role
     * @author Mini
     */
    public static void onMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (UsersInfo.isFirstConnection(event.getUser().getId())) onMemberJoin(new GuildMemberJoinEvent(event.getJDA(), event.getResponseNumber(), event.getMember()));
    }

    private static EmbedBuilder buildEmbed(Languages language) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(0x2d6401);
        embed.setTitle(TranslationManager.format(language.asString(), "discord.welcomeMessage.title"));
        embed.setDescription(TranslationManager.format(language.asString(), "discord.welcomeMessage.description") +"https://hunterz.fr/");
        embed.setImage("https://cdn.discordapp.com/attachments/1107399131464474776/1107399132756324352/2023-05-14_21.58.27.png");

        return embed;
    }
}

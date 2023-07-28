package com.photon.discord.slashCommands.advancedCommands;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.photon.discord.BotEngine;
import com.photon.discord.Roles;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CustomMute {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Role muteRole = BotEngine.guild.getRoleById(Roles.MUTE.id);

    private static HashMap<String, Integer> muteDuration = new HashMap<String, Integer>(){{
        put("10 minutes", 10);
        put("30 minutes", 30);
        put("1 hour", 60);
        put("6 hour", 360);
        put("1 day", 1440);
        put("1 week", 10080);
        put("1 month", 43200); // 30 days
    }};


    /**
     * Mute a user for a certain amount of time (add a role to the user)
     * @param user The user to mute
     * @param time The mute duration in minutes
     * @author Mini
     */
    private static void timeMute(User user, int time) {
        BotEngine.guild.addRoleToMember(user, muteRole).queue();
        scheduler.schedule(() -> BotEngine.guild.removeRoleFromMember(user, muteRole).queue(), time, TimeUnit.MINUTES);
        
    }



    /**
     * Mute a user for a certain amount of time (/tempmute user days hours minutes)
     * @param event The event that triggered this command
     * @author Mini
     */
    public static void tempmute(SlashCommandInteractionEvent event) {
        System.out.println(event.getOption("user"));
        final User player = event.getOption("user").getAsUser();
        final String time = event.getOption("duration").getAsString();
        final String reason = event.getOption("reason").getAsString();

        timeMute(player, (int) muteDuration.get(time));
        event.reply(String.format("Mute %s for %s, reason : %s", player.getName(), time, reason )).queue();

        //Dm the player
        player.openPrivateChannel().queue((channel) ->
        {   
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(0xfd1a1a);
            embed.setTitle("You have been muted !");
            embed.addField("Mute duration :", time, false);
            embed.addField("Reason :", reason, false);


            channel.sendMessageEmbeds(embed.build()).addActionRow(
                Button.link("https://hunterz.fr/", "Contest")
            ).queue();
        });
    }


}
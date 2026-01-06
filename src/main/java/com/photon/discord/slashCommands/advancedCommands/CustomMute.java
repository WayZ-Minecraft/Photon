package com.photon.discord.slashCommands.advancedCommands;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.photon.discord.BotEngine;
import com.photon.discord.Roles;
import com.photon.discord.slashCommands.AutoCompleteRegistry;
import com.photon.discord.usersInteraction.data.MutesInfo;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CustomMute {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);


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
     * Get the mute role from the guild
     * Lazy initialization to avoid null pointer when class is loaded
     * @return The mute role
     */
    private static Role getMuteRole() {
        if (BotEngine.guild == null) {
            throw new IllegalStateException("Guild not initialized yet");
        }
        return BotEngine.guild.getRoleById(Roles.MUTE.id);
    }
    /**
     * Register autocomplete providers for this command
     * Should be called during bot initialization
     */
    public static void registerAutoComplete() {
        AutoCompleteRegistry.registerFromCollection("tempmute", "duration", v -> muteDuration.keySet());
    }

    /**
     * Mute a user (add a role to the user)
     * @param user The user to mute
     */
    public static void mute(User user) {
        BotEngine.guild.addRoleToMember(user, getMuteRole()).queue();
    }

    /**
     * Unmute a user (remove mute role to the user)
     * @param user The user to unmute
     */
    public static void unmute(User user){
        BotEngine.guild.removeRoleFromMember(user, getMuteRole()).queue();
    }


    /**
     * Mute a user for a certain amount of time (add a role to the user)
     * @param user The user to mute
     * @param time The mute duration in minutes
     * @author Mini
     */
    private static void timeMute(User user, int time) {
        mute(user);
        MutesInfo.addUser(user.getId(), time);

        // Unmute the user after the time
        activeUnmute(user, time);
        
    }
    

    /**
     * Unmute a user after a certain amount of time
     * @param user The user to unmute
     * @param time The mute duration in minutes
     * @author Mini
     * 
     * @see {@link #timeMute(User, int)} to mute a user
     * @note this method is used for reactivating the unmute after a bot restart
     */
    public static void activeUnmute(User user, int time){
        scheduler.schedule(() -> {
            MutesInfo.removeUser(user.getId());
        }, time, TimeUnit.MINUTES);
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

    /**
     * Voice mute all user in the voice channel of the user who triggered the command
     * @param event The event that triggered this command
     */
    public static void silence(SlashCommandInteractionEvent event) {
        int time = event.getOption("duration") == null ? 5 : (int) event.getOption("duration").getAsInt();
        event.reply("Silence !").queue();
        event.getGuild().getVoiceChannelById(event.getMember().getVoiceState().getChannel().getId()).getMembers().forEach(member -> {
            if(!member.getUser().isBot()){
                member.mute(true).queue();
            }
        });

        scheduler.schedule(() -> {
            event.getGuild().getVoiceChannelById(event.getMember().getVoiceState().getChannel().getId()).getMembers().forEach(member -> {
                if(!member.getUser().isBot()){
                    member.mute(false).queue();
                }
            });
        }, time, TimeUnit.SECONDS);
    }


}
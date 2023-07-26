package com.photon.discord.advancedCommands;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.photon.discord.BotEngine;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class CustomMute {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static final Long MUTE_ROLE_ID = 1133846380994105374L; // TODO: Change this to your mute role id
    private static final Role muteRole = BotEngine.guild.getRoleById(MUTE_ROLE_ID);



    /**
     * Mute a user for a certain amount of time (add a role to the user)
     * @param user The user to mute
     * @param time The mute duration in minutes
     * @author Mini
     */
    public static void timeMute(User user, int time) {
        BotEngine.guild.addRoleToMember(user, muteRole).queue();
        scheduler.schedule(() -> BotEngine.guild.removeRoleFromMember(user, muteRole).queue(), time, TimeUnit.MINUTES);
        
    }

}
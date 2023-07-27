package com.photon.discord.advancedCommands;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.photon.discord.BotEngine;
import com.photon.discord.Roles;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class CustomMute {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Role muteRole = BotEngine.guild.getRoleById(Roles.MUTE.id);



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
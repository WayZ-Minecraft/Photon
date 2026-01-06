package com.photon.discord.usersInteraction.data;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.photon.discord.BotEngine;
import com.photon.discord.InfoType;
import com.photon.discord.MuteObject;
import com.photon.discord.ObjectDiscord;
import com.photon.discord.MuteObject.MuteInfo;
import com.photon.discord.slashCommands.advancedCommands.CustomMute;

/**
 * Class to manage user information, this class is used to mute users
 * 
 * @see {@link com.photon.discord.MuteObject}
 * @author Mini
 */
public class MutesInfo {
    public static MuteObject muteData;
    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);

    /**
     * Init the the mute information, reactive all the unmute sheedule and start the checkMute sheedule
     */
    public static void init() {
        load();
        reactiveAllUnmute();
        scheduler.scheduleAtFixedRate(() -> checkMute(60), 1, 1, TimeUnit.HOURS);
    }

    /**
     * Load the user information
     */
    public static void load() {
        muteData = (MuteObject)ObjectDiscord.load(InfoType.MUTE);
    }

    /**
     * Save the user information
     */
    public static void save() {
        ObjectDiscord.save(muteData, InfoType.MUTE);
    }

    /**
     * Add a user to the user information, if the user already exist, it will take the highest time mute
     * @param id the id of the user
     * @param time the time of the mute
     */
    public static void addUser(String id, int time) {
        if (!muteData.muteUsers.containsKey(id)) {
            final MuteInfo muteInfo = muteData.new MuteInfo();
            muteInfo.time = time;
            MutesInfo.muteData.muteUsers.put(id, muteInfo);
        }
        else {
            if (muteData.muteUsers.get(id).time < time) {
                muteData.muteUsers.get(id).time = time;
            }
        }
        save();
    }

    /**
     * Remove a user from the user mute information and unmute him
     * @param id the id of the user
     */
    public static void removeUser(String id) {
        if (muteData.muteUsers.containsKey(id)) {
            muteData.muteUsers.remove(id);
            CustomMute.unmute(BotEngine.guild.getMemberById(id).getUser());
        }
        save();
    }

    /**
     * Reset all the mute time of the mute users
     * @param time the time before last check time
     */
    private static void checkMute(int time){
        for (String id : muteData.muteUsers.keySet()) {
            if (muteData.muteUsers.get(id).time <= time) {
                removeUser(id);
            }
            else {
                muteData.muteUsers.get(id).time -= time;
            }
        }
        save();
    }

    /**
     * Reactive all the unmute sheedule
     * 
     * @note This function is called when the bot is started
     */
    private static void reactiveAllUnmute() {
        for (String id : muteData.muteUsers.keySet()) {
            CustomMute.activeUnmute(BotEngine.guild.getMemberById(id).getUser(), muteData.muteUsers.get(id).time);
        }
        save();
    }
}

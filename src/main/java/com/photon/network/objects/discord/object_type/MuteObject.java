package com.photon.network.objects.discord.object_type;

import java.util.HashMap;

import com.photon.network.objects.discord.ObjectDiscord;

/**
 * Class that contains the mute information of the users in the discord server
 */
public class MuteObject extends ObjectDiscord{

    public HashMap<String, MuteInfo> muteUsers = new HashMap<>();

    public class MuteInfo {
        public boolean mute = true;
        public int time = 10;
    }
    
}

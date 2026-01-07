package com.photon.discord;

import java.util.HashMap;

/**
 * Class that contains the mute information of the users in the discord server
 */
public class MuteObject extends ObjectDiscord {

    public HashMap<String, MuteInfo> muteUsers = new HashMap<>();

    public class MuteInfo {
        public boolean mute = true;
        public int time = 10;
    }
    
}

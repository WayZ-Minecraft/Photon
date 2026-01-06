package com.photon.discord;

import java.util.ArrayList;
import java.util.HashMap;

import com.photon.discord.usersInteraction.language.Languages;

/**
 * Class that contains the global information about user for the Discord bot
 */
public class GlobalObject extends ObjectDiscord {
    public HashMap<String, UserInfo> Users = new HashMap<>();



    /**
     * Class that contains the information about a user
     */
    public class UserInfo {
        public boolean firstConnection = true;
        public ArrayList<Languages> language = new ArrayList<>();
    }
}

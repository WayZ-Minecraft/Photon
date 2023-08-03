package com.photon.network.objects.discord.object_type;

import java.util.ArrayList;
import java.util.HashMap;

import com.photon.discord.usersInteraction.language.Languages;
import com.photon.network.objects.discord.ObjectDiscord;

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

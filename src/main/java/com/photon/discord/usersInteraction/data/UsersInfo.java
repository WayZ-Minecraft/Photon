package com.photon.discord.usersInteraction.data;

import java.util.ArrayList;
import java.util.Arrays;

import com.photon.discord.usersInteraction.Languages;
import com.photon.network.objects.discord.InfoType;
import com.photon.network.objects.discord.ObjectDiscord;
import com.photon.network.objects.discord.object_type.GlobalObject;
import com.photon.network.objects.discord.object_type.GlobalObject.UserInfo;
import com.photon.util.ConsoleManager;

/**
 * Class to manage user information
 * 
 * @see {@link com.photon.network.objects.discord.object_type.GlobalObject}
 * @author Mini
 */
public class UsersInfo {
    public static GlobalObject globalInfo;


    /**
     * Init the user information
     * 
     * @note He load all the user information (mute, global, etc...)
     */
    public static void init() {
        load();
        MutesInfo.init();
    }

    /**
     * Load the user information
     */
    public static void load() {
        globalInfo = (GlobalObject)ObjectDiscord.load(InfoType.GLOBAL);
    }

    /**
     * Save the user information
     */
    public static void save() {
        ObjectDiscord.save(globalInfo, InfoType.GLOBAL);
    }

    /**
     * Add a user to the user information, if the user already exist, it will do nothing
     * @param id the id of the user
     */
    public static void addUser(String id) {
        if (!globalInfo.Users.containsKey(id)) {
            UserInfo userInfo = globalInfo.new UserInfo();
            globalInfo.Users.put(id, userInfo);
        }
        save();
    }

    /**
     * Check if it's the first connection of the user
     * 
     * @param id the id of the user
     * @return boolean : true if it's the first connection, false if not
     */
    public static boolean isFirstConnection(String id) {
        if (globalInfo.Users.containsKey(id)) {
            return globalInfo.Users.get(id).firstConnection;
        } else {
            return true;
        }
    }

    /**
     * Set if it's the first connection of the user
     * @param id the id of the user
     * @param firstConnection true if it's the first connection, false if not
     */
    public static void setFirstConnection(String id, boolean firstConnection) {
        if (globalInfo.Users.containsKey(id)) {
            globalInfo.Users.get(id).firstConnection = firstConnection;
        } else {
            addUser(id);
            setFirstConnection(id, firstConnection);
        }
        save();
    }

    /**
     * Get the language of the user
     * @param id the id of the user
     * @return Languages[] : the language of the user
     */
    public static ArrayList<Languages> getLanguage(String id) {
        if (globalInfo.Users.containsKey(id)) {
            return globalInfo.Users.get(id).language;
        } else {
            ConsoleManager.create("Error while getting language of " + id).displayOnDiscord().error().end();
            addUser(id);
            return new ArrayList<Languages>(){{
                add(Languages.ENGLISH);
            }};
        }
    }

    /**
     * Set the language of the user
     * @param id the id of the user
     * @param language the language of the user
     */
    public static void setLanguage(String id, Languages[] language) {
        if (globalInfo.Users.containsKey(id)) {
            globalInfo.Users.get(id).language = new ArrayList<Languages>(Arrays.asList(language));
        } else {
            addUser(id);
            setLanguage(id, language);
        }
        save();
    }

    /**
     * Add languages to the user
     * @param id the id of the user
     * @param languages the languages to add
     * 
     * @note if the user already have the language, it will do nothing
     */
    public static void addLanguages(String id, Languages... languages) {
        if (globalInfo.Users.containsKey(id)) {
            for (Languages language : languages) {
                if (!globalInfo.Users.get(id).language.contains(language)) {
                    globalInfo.Users.get(id).language.add(language);
                }
            }
        } else {
            addUser(id);
            addLanguages(id, languages);
        }
        save();
    }

    /**
     * Remove languages to the user
     * @param id the id of the user
     * @param languages the languages to remove
     * 
     * @note if the user doesn't have the language, it will do nothing
     */
    public static void removeLanguages(String id, Languages... languages) {
        if (globalInfo.Users.containsKey(id)) {
            for (Languages language : languages) {
                if (globalInfo.Users.get(id).language.contains(language)) {
                    globalInfo.Users.get(id).language.remove(language);
                }
            }
        } else {
            addUser(id);
            removeLanguages(id, languages);
        }
        save();
    }




    /**
     * Remove a user from the database
     * @param id the id of the user
     */
    public static void removeUser(String id) {
        globalInfo.Users.remove(id);
    }

}

package com.photon.discord.usersInteraction.data;

import java.util.ArrayList;
import java.util.Arrays;

import com.photon.discord.usersInteraction.language.Languages;
import com.photon.network.objects.discord.InfoType;
import com.photon.network.objects.discord.ObjectDiscord;
import com.photon.network.objects.discord.object_type.GlobalObject;
import com.photon.network.objects.discord.object_type.GlobalObject.UserInfo;
import com.photon.network.sql.SQLxp;
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
     * Remove a user from the database
     * @param id the id of the user
     */
    public static void removeUser(String id) {
        globalInfo.Users.remove(id);
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
        }
        save();
    }

    /**
     * Add xp to the user, if the user have enough xp to level up, it will level up
     * @param id the id of the user
     * @param xp the xp to add
     */
    public static void addXp(String id, int xp) {
        int xpToNextLevel = getXpToNextLevel(id);
        final int userXp = getXp(id);
        int level = getLevel(id);
        if (userXp + xp >= xpToNextLevel) {
            while (userXp + xp >= xpToNextLevel) {
                xp -= xpToNextLevel;
                level++;
                xpToNextLevel = SQLxp.getXpLevel(level);
            }
            SQLxp.setLevel(id, level);
            SQLxp.setXp(id, userXp + xp);
        } else {
            SQLxp.addXp(id, xp);
        }
    }

    /**
     * Remove xp to the user, if the user have not enough xp to level down, it will level down
     * @param id the id of the user
     * @param xp the xp to remove
     */
    public static void removeXp(String id, int xp) {
        int xpToNextLevel = getXpToNextLevel(id);
        final int userXp = getXp(id);
        int level = getLevel(id);
        if (userXp - xp < 0) {
            while (userXp - xp < 0 && level > 1) {
                level--;
                xpToNextLevel = SQLxp.getXpLevel(level);
                xp -= xpToNextLevel;
            }

            if (level == 1 && userXp - xp < 0) {SQLxp.setXp(id, 0); SQLxp.setLevel(id, 1);}
            else {
                SQLxp.setXp(id, userXp - xp);
                SQLxp.setLevel(id, level);
            }
        }
        else {
            SQLxp.addXp(id, -xp);;
        }
    }

    /**
     * Get the xp of the user
     * @param id the id of the user
     * @return int : the xp of the user
     */
    public static int getXp(String id) {
        return SQLxp.getXp(id);
    }

    /**
     * Get the rank of the user
     * @param id the id of the user
     * @return int : the rank of the user (-1 if sql error)
     */
    public static int getRank(String id) {
        return SQLxp.getRank(id);
    }

    /**
     * Add a level to the user
     * @param id the id of the user
     */
    public static void addLevel(String id) {
        SQLxp.addLevel(id, 1);
    }

    /**
     * Remove a level to the user
     * @param id the id of the user
     */
    public static void removeLevel(String id) {
        if (getLevel(id) > 1) SQLxp.addLevel(id, -1);
        
    }

    /**
     * Get the level of the user
     * @param id the id of the user
     * @return int : the level of the user (-1 if sql error)
     */
    public static int getLevel(String id) {
        return SQLxp.getLevel(id);
    }

    /**
     * Get the xp to the next level of the user
     * @param id the id of the user
     * @return int : the xp to the next level of the user (-1 if sql error)
     */
    public static int getXpToNextLevel(String id) {
        return SQLxp.getXpLevel(getLevel(id));
    }
}

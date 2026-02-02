package com.photon.discord.language;

import java.util.ArrayList;

import com.photon.discord.BotEngine;
import com.photon.discord.Roles;
import com.photon.network.sql.SQLDiscordProfile;
import com.photon.util.NetworkOnly;

/**
 * Class to manage user information
 * 
 * @see {@link com.photon.discord.GlobalObject}
 * @author Mini
 */
@Deprecated //TODO use SQLDiscordProfile directly
@NetworkOnly
public class UsersInfo {
    
    /**
     * Get the language of the user
     * @param id the id of the user
     * @return Languages[] : the language of the user
     */
    public static ArrayList<Languages> getLanguages(String id) {
        ArrayList<Languages> languages = SQLDiscordProfile.getLanguages(id);
        if (languages == null) {
            final ArrayList<Languages> language = new ArrayList<Languages>();
            BotEngine.guild.getMemberById(id).getRoles().forEach(role -> {
                if (role.getIdLong() == Roles.FR.id) language.add(Languages.FRENCH);
                else if (role.getIdLong() == Roles.EN.id) language.add(Languages.ENGLISH);
            });
            SQLDiscordProfile.setLanguages(id, language);
            return language;
        }
        
        return languages;      
    }

    /**
     * Get the first language of the user
     * @param id the id of the user
     * @return Languages : the first language of the user, if the user doesn't have any language, it will return english
     */
    public static Languages getLanguage(String id) {
        ArrayList<Languages> userFirstLanguage = getLanguages(id);
        if (userFirstLanguage.isEmpty()) return Languages.ENGLISH;
        else return userFirstLanguage.get(0);
    }

    /**
     * Add languages to the user
     * @param id the id of the user
     * @param languages the languages to add
     * 
     * @note if the user already have the language, it will do nothing
     */
    public static void addLanguages(String id, Languages... languages) {
        ArrayList<Languages> userLanguages = SQLDiscordProfile.getLanguages(id);
        if (userLanguages == null) {
            getLanguages(id); // Will add all the languages roles of the user
            return;
        }
        for (Languages language : languages) {
            if (!userLanguages.contains(language)) {
                userLanguages.add(language);
            }
        }
        SQLDiscordProfile.setLanguages(id, userLanguages);
    }

    /**
     * Remove languages to the user
     * @param id the id of the user
     * @param languages the languages to remove
     * 
     * @note if the user doesn't have the language, it will do nothing
     */
    public static void removeLanguages(String id, Languages... languages) {
        ArrayList<Languages> userLanguages = SQLDiscordProfile.getLanguages(id);
        if (userLanguages == null) {
            getLanguages(id); // Will add all the languages roles of the user
            return;
        }
        for (Languages language : languages) {
            if (userLanguages.contains(language)) {
                userLanguages.remove(language);
            }
        }
        SQLDiscordProfile.setLanguages(id, userLanguages);
    }
}

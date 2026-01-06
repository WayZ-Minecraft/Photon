package com.photon.discord.usersInteraction.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.photon.discord.BotEngine;
import com.photon.discord.Roles;
import com.photon.discord.usersInteraction.language.Languages;
import com.photon.network.sql.SQLuser;
import com.photon.network.sql.SQLdiscordXp;
import com.photon.util.ConsoleManager;

/**
 * Class to manage user information
 * 
 * @see {@link com.photon.discord.GlobalObject}
 * @author Mini
 */
public class UsersInfo {

    /**
     * Check if it's the first connection of the user
     * 
     * @param id the id of the user
     * @return boolean : true if it's the first connection, false if not
     */
    public static boolean isFirstConnection(String id) {
        return SQLuser.getFirstConnection(id);
    }

    /**
     * Set if it's the first connection of the user
     * @param id the id of the user
     * @param firstConnection true if it's the first connection, false if not
     */
    public static void setFirstConnection(String id, boolean firstConnection) {
        try {
            SQLuser.setFirstConnection(id, firstConnection);
        } catch (SQLException e) {
            ConsoleManager.create("Error while setting first connection of user: " + id + e).displayOnDiscord().error().end();
        }
    }

    /**
     * Get the language of the user
     * @param id the id of the user
     * @return Languages[] : the language of the user
     */
    public static ArrayList<Languages> getLanguages(String id) {
        try {
            ArrayList<Languages> languages = SQLuser.getLanguages(id);
            if (languages == null) {
                final ArrayList<Languages> language = new ArrayList<Languages>();
                BotEngine.guild.getMemberById(id).getRoles().forEach(role -> {
                    if (role.getIdLong() == Roles.FR.id) language.add(Languages.FRENCH);
                    else if (role.getIdLong() == Roles.EN.id) language.add(Languages.ENGLISH);
                });
                SQLuser.setLanguages(id, language);
                return language;
            }
            
            return languages;      
        } catch (SQLException e) {
            ConsoleManager.create("Error while getting languages of user: " + id + e).displayOnDiscord().error().end();
            return new ArrayList<Languages>();
        }
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
     * Set the language of the user
     * @param id the id of the user
     * @param language the language of the user
     */
    public static void setLanguage(String id, Languages[] language) {
        try {
            SQLuser.setLanguages(id, new ArrayList<Languages>(Arrays.asList(language)));
        } catch (SQLException e) {
            ConsoleManager.create("Error while setting language of user: " + id + e).displayOnDiscord().error().end();
        }
    }

    /**
     * Add languages to the user
     * @param id the id of the user
     * @param languages the languages to add
     * 
     * @note if the user already have the language, it will do nothing
     */
    public static void addLanguages(String id, Languages... languages) {
        try {
            ArrayList<Languages> userLanguages = SQLuser.getLanguages(id);
            if (userLanguages == null) {
                getLanguages(id); // Will add all the languages roles of the user
                return;
            }
            for (Languages language : languages) {
                if (!userLanguages.contains(language)) {
                    userLanguages.add(language);
                }
            }
            SQLuser.setLanguages(id, userLanguages);
        } catch (SQLException e) {
            ConsoleManager.create("Error while adding languages to user: " + id + e).displayOnDiscord().error().end();
        }
    }

    /**
     * Remove languages to the user
     * @param id the id of the user
     * @param languages the languages to remove
     * 
     * @note if the user doesn't have the language, it will do nothing
     */
    public static void removeLanguages(String id, Languages... languages) {
        try {
            ArrayList<Languages> userLanguages = SQLuser.getLanguages(id);
            if (userLanguages == null) {
                getLanguages(id); // Will add all the languages roles of the user
                return;
            }
            for (Languages language : languages) {
                if (userLanguages.contains(language)) {
                    userLanguages.remove(language);
                }
            }
            SQLuser.setLanguages(id, userLanguages);
        } catch (SQLException e) {
            ConsoleManager.create("Error while removing languages to user: " + id + e).displayOnDiscord().error().end();
        }
    }

    /**
     * Add xp to the user, if the user have enough xp to level up, it will level up
     * @param id the id of the user
     * @param xp the xp to add
     * 
     * @throws SQLException
     */
    public static void addXp(String id, int xp) throws SQLException {
        int xpToNextLevel = getXpToNextLevel(id);
        final int userXp = getXp(id);
        int level = getLevel(id);
        if (userXp + xp >= xpToNextLevel) {
            while (userXp + xp >= xpToNextLevel) {
                xp -= xpToNextLevel;
                level++;
                xpToNextLevel = SQLdiscordXp.getXpLevel(level);
            }
            SQLdiscordXp.setLevel(id, level);
            SQLdiscordXp.setXp(id, userXp + xp);
        } else {
            SQLdiscordXp.addXp(id, xp);
        }
    }

    /**
     * Remove xp to the user, if the user have not enough xp to level down, it will level down
     * @param id the id of the user
     * @param xp the xp to remove
     * 
     * @throws SQLException
     */
    public static void removeXp(String id, int xp) throws SQLException {
        int xpToNextLevel = getXpToNextLevel(id);
        final int userXp = getXp(id);
        int level = getLevel(id);
        if (userXp - xp < 0) {
            while (userXp - xp < 0 && level > 1) {
                level--;
                xpToNextLevel = SQLdiscordXp.getXpLevel(level);
                xp -= xpToNextLevel;
            }

            if (level == 1 && userXp - xp < 0) {SQLdiscordXp.setXp(id, 0); SQLdiscordXp.setLevel(id, 1);}
            else {
                SQLdiscordXp.setXp(id, userXp - xp);
                SQLdiscordXp.setLevel(id, level);
            }
        }
        else {
            SQLdiscordXp.addXp(id, -xp);;
        }
    }

    /**
     * Get the xp of the user
     * @param id the id of the user
     * @return int : the xp of the user
     */
    public static int getXp(String id) throws SQLException {
        return SQLdiscordXp.getXp(id);
    }

    /**
     * Get the rank of the user
     * @param id the id of the user
     * @return int : the rank of the user (-1 if sql error)
     */
    public static int getRank(String id) throws SQLException {
        return SQLdiscordXp.getRank(id);
    }

    /**
     * Add a level to the user
     * @param id the id of the user
     */
    public static void addLevel(String id) {
        SQLdiscordXp.addLevel(id, 1);
    }

    /**
     * Remove a level to the user
     * @param id the id of the user
     */
    public static void removeLevel(String id) throws SQLException {
        if (getLevel(id) > 1) SQLdiscordXp.addLevel(id, -1);
        
    }

    /**
     * Get the level of the user
     * @param id the id of the user
     * @return int : the level of the user (-1 if sql error)
     */
    public static int getLevel(String id) throws SQLException {
        return SQLdiscordXp.getLevel(id);
    }

    /**
     * Get the xp to the next level of the user
     * @param id the id of the user
     * @return int : the xp to the next level of the user (-1 if sql error)
     */
    public static int getXpToNextLevel(String id) throws SQLException {
        return SQLdiscordXp.getXpLevel(getLevel(id));
    }
}

package com.photon.discord.usersInteraction;

import java.util.regex.Pattern;

public class Security {
    
    /**
     * Check if String contains a link
     * @param link String to check
     * @return true if String contains a link
     */
    public static boolean checkLink(String link) {
        Pattern pattern = Pattern.compile(".*(https?://)?([a-z0-9_\\-]+\\.)+[a-z]{2,}(\\S*)?.*");
        if (pattern.matcher(link).matches()) return true;
        else return false;
    }

}

package com.photon.discord;

import java.util.regex.Pattern;

import com.photon.util.NetworkOnly;

@NetworkOnly
public class DiscordSecurity {
    
    /**
     * Check if String contains a link
     * @param link String to check
     * @return true if String contains a link
     */
    public static boolean checkLink(String link) {
        final Pattern LINK_PATTERN = Pattern.compile(".*(https?://)?([a-z0-9_\\-]+\\.)+[a-z]{2,}(\\S*)?.*");
        if (LINK_PATTERN.matcher(link).matches()) return true;
        else return false;
    }
}

package com.photon.discord.usersInteraction;

/**
 * Enum to manage the languages
 * @author Mini
 */
public enum Languages {
    FRENCH("fr"),
    ENGLISH("en");

    public String code;

    private Languages(String code) {
        this.code = code;
    }

    public String asString() {
        return this.code;
    }


    /**
     * Get the language enum value from the string code
     * @param code the string code (ex: "fr")
     * @return Languages : the language enum value
     * @author Mini
     */
    public static Languages getLanguage(String code) {
        for (Languages language : Languages.values()) {
            if (language.code.equals(code)) return language;
        }
        return null;
    }
}

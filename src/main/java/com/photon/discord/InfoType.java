package com.photon.discord;

/**
 * Enum that contains all the type of information that use the Discord bot
 * Each type of information have a file
 */
public enum InfoType {
    GLOBAL("global", new GlobalObject()),
    MUTE("mute", new MuteObject());

    private String fileName;
    private Object objectClass;

    /**
     * Constructor
     * @param fileName the name of the file
     * @param objectClass an instence from class of the object
     */
    InfoType(String fileName, Object objectClass) {
        this.fileName = fileName;
        this.objectClass = objectClass;
    }

    /**
     * Get the name of the file
     * @return the name of the file
     */
    public String getPath() {
        return fileName;
    }

    /**
     * Get an instence from class of the object
     * @return an instence from class of the object
     */
    public Object getObjectClass() {
        return objectClass;
    }
}

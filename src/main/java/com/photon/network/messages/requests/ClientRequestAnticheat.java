package com.photon.network.messages.requests;

/**
 * @author noz43
 */
public class ClientRequestAnticheat {
    private final String fileName;
    private final String fileMessage;
    private final String operatingSystem;
    private final String userUUID;
    
    public ClientRequestAnticheat(String fileName, String fileMessage, String operatingSystem, String userUUID) {
        this.fileName = fileName;
        this.fileMessage = fileMessage;
        this.operatingSystem = operatingSystem;
        this.userUUID = userUUID;
    }
    
    public String getFileName() { return fileName; }
    public String getFileMessage() { return fileMessage; }
    public String getOperatingSystem() { return operatingSystem; }
    public String getUserUUID() { return userUUID; }
}
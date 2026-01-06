package com.photon.network.messages.requests;

/**
 * @author noz43
 */
public class ClientRequestCrashReport {
    private final String fileMessage;
    private final String fileName;
    private final String userUUID;
    
    public ClientRequestCrashReport(String fileMessage, String fileName, String userUUID) {
        this.fileMessage = fileMessage;
        this.fileName = fileName;
        this.userUUID = userUUID;
    }
    
    public String getFileMessage() { return fileMessage; }
    public String getFileName() { return fileName; }
    public String getUserUUID() { return userUUID; }
}
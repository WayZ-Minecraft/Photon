package com.photon.network.messages.requests;

/**
 * @author noz43
 */
public class ClientRequestHWID {
    private final String userName;
    private final String userUUID;
    private final String userHWID;
    private final String operatingSystem;
    
    public ClientRequestHWID(String userName, String userUUID, String userHWID, String operatingSystem) {
        this.userName = userName;
        this.userUUID = userUUID;
        this.userHWID = userHWID;
        this.operatingSystem = operatingSystem;
    }
    
    public String getUserName() { return userName; }
    public String getUserUUID() { return userUUID; }
    public String getUserHWID() { return userHWID; }
    public String getOperatingSystem() { return operatingSystem; }
}
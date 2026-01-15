package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.sql.SQLHWID;
import com.photon.network.IPacket;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class ClientRequestHWID implements IPacket {
    private final String userName;
    private final String userUUID;
    private final String userHWID;
    private final String operatingSystem;
    
    public ClientRequestHWID() {
        this.userName = "";
        this.userUUID = "";
        this.userHWID = "";
        this.operatingSystem = "";
    }

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
    
    @Override
    public void handle(Connection connection) {
        SQLHWID.saveHWID(userName, userUUID, userHWID, operatingSystem);
        
        ConsoleManager.create("HWID received: " + userUUID + 
            "\nOS: " + operatingSystem + 
            "\nHWID: " + userHWID)
            .withType(EnumLogType.NETWORK)
            .end();
    }
}
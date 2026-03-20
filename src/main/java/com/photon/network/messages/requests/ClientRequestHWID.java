package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.IPacket;
import com.photon.sql.HWIDTable;
import com.photon.util.PhotonLogTypes;
import com.photon.util.os.OperatingSystem;

import niwer.lumen.Console;

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

    public ClientRequestHWID(String userName, String userUUID, String userHWID, OperatingSystem operatingSystem) {
        this.userName = userName;
        this.userUUID = userUUID;
        this.userHWID = userHWID;
        this.operatingSystem = operatingSystem.NAME;
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
        HWIDTable.save(userName, userUUID, userHWID, operatingSystem);
        
        Console.log("HWID received: " + userUUID + " OS: " + operatingSystem + " HWID: " + userHWID).type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
    }
}
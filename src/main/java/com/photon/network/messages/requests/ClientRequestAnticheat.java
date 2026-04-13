package com.photon.network.messages.requests;

import java.util.UUID;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.IPacket;
import com.photon.sql.AnticheatTable;
import com.photon.util.PhotonLogTypes;
import com.photon.util.os.OperatingSystem;

import niwer.lumen.Console;

/**
 * @author Niwer
 * @author noz43
 */
public class ClientRequestAnticheat implements IPacket {
    private final String fileName;
    private final String fileMessage;
    private final String operatingSystem;
    private final String userUUID;
    
    public ClientRequestAnticheat() {
        this.fileName = "";
        this.fileMessage = "";
        this.operatingSystem = "";
        this.userUUID = "";
    }

    public ClientRequestAnticheat(String fileName, String fileMessage, UUID userUUID) {
        this(fileName, fileMessage, OperatingSystem.currentPlatform(), userUUID.toString().replace("-", ""));
    }

    public ClientRequestAnticheat(String fileName, String fileMessage, OperatingSystem operatingSystem, String userUUID) {
        this.fileName = fileName;
        this.fileMessage = fileMessage;
        this.operatingSystem = operatingSystem.toString();
        this.userUUID = userUUID;
    }

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
    
    @Override
    public void handle(Connection connection) {
        AnticheatTable.save(userUUID, fileName, fileMessage, operatingSystem);
        Console.log("Cheater detected: " + userUUID + " OS: " + operatingSystem + " Reason: " + fileMessage).sendToProcessor().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER)
        .send();
    }
}
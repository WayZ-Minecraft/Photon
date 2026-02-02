package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.sql.SQLAnticheat;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.os.OperatingSystem;

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
        SQLAnticheat.save(userUUID, fileName, fileMessage, operatingSystem);
        ConsoleManager.create("Cheater detected: " + userUUID + " OS: " + operatingSystem + " Reason: " + fileMessage).displayOnDiscord().withType(EnumLogType.NETWORK).end();
    }
}
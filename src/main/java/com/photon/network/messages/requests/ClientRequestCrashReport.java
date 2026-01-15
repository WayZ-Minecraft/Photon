package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.sql.SQLCrashReport;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author Niwer
 * @author noz43
 */

public class ClientRequestCrashReport implements IPacket {
    private final String fileMessage;
    private final String fileName;
    private final String userUUID;
    
    public ClientRequestCrashReport() {
        this.fileMessage = "";
        this.fileName = "";
        this.userUUID = "";
    }

    public ClientRequestCrashReport(String fileMessage, String fileName, String userUUID) {
        this.fileMessage = fileMessage;
        this.fileName = fileName;
        this.userUUID = userUUID;
    }
    
    public String getFileMessage() { return fileMessage; }
    public String getFileName() { return fileName; }
    public String getUserUUID() { return userUUID; }
    
    @Override
    public void handle(Connection connection) {
        SQLCrashReport.saveCrashReport(userUUID, fileName, fileMessage);
        
        ConsoleManager.create("Crash report received: " + userUUID)
            .displayOnDiscord()
            .withType(EnumLogType.NETWORK)
            .end();
    }
}
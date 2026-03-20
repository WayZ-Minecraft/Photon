package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.IPacket;
import com.photon.sql.CrashReportTable;
import com.photon.util.PhotonLogTypes;

import niwer.lumen.Console;

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
        CrashReportTable.save(userUUID, fileName, fileMessage);
        
        
        Console.log("Crash report received: " + userUUID)
            .sendToProcessor()
            .type(PhotonLogTypes.NETWORK)
            .container(PhotonEngine.LOGGER)
            .send();
    }
}
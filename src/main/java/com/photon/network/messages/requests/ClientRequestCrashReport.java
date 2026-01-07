package com.photon.network.messages.requests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.NetworkDirectories;
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
        try {
            File crashFolder = new File(NetworkDirectories.crashDirectory, userUUID);
            File crashReportFile = new File(crashFolder, fileName + ".txt");
            
            if (!crashFolder.exists()) {
                crashFolder.mkdirs();
            }
            
            if (!crashReportFile.exists()) {
                crashReportFile.createNewFile();
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(crashReportFile))) {
                writer.write(fileMessage);
            }
            
            ConsoleManager.create("Crash report received: " + userUUID)
                .displayOnDiscord()
                .withType(EnumLogType.NETWORK)
                .withFile(crashReportFile)
                .end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
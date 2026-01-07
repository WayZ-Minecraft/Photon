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
public class ClientRequestAnticheat implements IPacket {
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
    
    @Override
    public void handle(Connection connection) {
        try {
            File anticheatFolder = new File(NetworkDirectories.anticheatDirectory, userUUID);
            File anticheatFile = new File(anticheatFolder, fileName + ".txt");
            
            if (!anticheatFolder.exists()) {
                anticheatFolder.mkdirs();
            }
            
            if (!anticheatFile.exists()) {
                anticheatFile.createNewFile();
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(anticheatFile))) {
                writer.write(fileMessage);
                writer.write("\nOperating System: " + operatingSystem);
            }
            
            ConsoleManager.create("Cheater detected: " + userUUID + 
                "\nOS: " + operatingSystem + 
                "\nReason: " + fileMessage)
                .displayOnDiscord()
                .withType(EnumLogType.NETWORK)
                .end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
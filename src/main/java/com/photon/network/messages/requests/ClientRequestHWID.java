package com.photon.network.messages.requests;

import java.io.File;
import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.NetworkDirectories;
import com.photon.network.objects.ObjectHWIDs;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author Niwer
 * @author noz43
 */
public class ClientRequestHWID implements IPacket {
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
    
    @Override
    public void handle(Connection connection) {
        try {
            File HWIDsFile = new File(NetworkDirectories.baseDirectory, "HWIDs.json");
            
            if (!HWIDsFile.exists()) {
                HWIDsFile.createNewFile();
                ObjectHWIDs.create(HWIDsFile);
            }
            
            ObjectHWIDs hwids = ObjectHWIDs.load(HWIDsFile);
            if (hwids == null) {
                hwids = new ObjectHWIDs();
            }
            
            hwids.hwids.add(new ObjectHWIDs.HWID(userName, userUUID, userHWID, operatingSystem));
            
            hwids.save(HWIDsFile);
            
            ConsoleManager.create("HWID received: " + userUUID + 
                "\nOS: " + operatingSystem + 
                "\nHWID: " + userHWID)
                .withType(EnumLogType.NETWORK)
                .end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
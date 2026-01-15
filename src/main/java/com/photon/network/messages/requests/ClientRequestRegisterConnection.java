package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.NetworkEngine;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author Niwer
 * @author noz43
 */
public class ClientRequestRegisterConnection implements IPacket {
    private final String uuid;
    
    public ClientRequestRegisterConnection() {
        this.uuid = "";
    }

    public ClientRequestRegisterConnection(String uuid) {
        this.uuid = uuid;
    }
    
    public String getUuid() { return uuid; }
    
    @Override
    public void handle(Connection connection) {
        NetworkEngine.CONNECTED_CLIENTS_LIST.remove(uuid);
        NetworkEngine.CONNECTED_CLIENTS_LIST.put(uuid, connection);
        
        ConsoleManager.create("Connection registered: " + uuid)
            .withType(EnumLogType.NETWORK)
            .end();
    }
}
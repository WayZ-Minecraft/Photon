package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.NetworkEngine;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author noz43
 */

public class ClientRequestRegisterConnection {
    private final String uuid;
    
    public ClientRequestRegisterConnection(String uuid) {
        this.uuid = uuid;
    }
    
    public String getUuid() { return uuid; }
    
    public void handle(Connection connection) {
        NetworkEngine.CONNECTED_CLIENTS_LIST.remove(uuid);
        NetworkEngine.CONNECTED_CLIENTS_LIST.put(uuid, connection);
        
        ConsoleManager.create("Connection registered: " + uuid)
            .withType(EnumLogType.NETWORK)
            .end();
    }
}
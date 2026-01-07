package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.NetworkDirectories;
import com.photon.network.messages.response.ServerResponseNetworkConfig;

/**
 * @author noz43
 */

public class ClientRequestNetworkConfig {
    
    public void handle(Connection connection) {
        ServerResponseNetworkConfig response = new ServerResponseNetworkConfig(NetworkDirectories.getConfig());
        connection.sendTCP(response);
    }
}
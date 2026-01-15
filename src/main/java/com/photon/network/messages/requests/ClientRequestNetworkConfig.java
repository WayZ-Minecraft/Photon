package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.NetworkDirectories;
import com.photon.network.messages.response.ServerResponseNetworkConfig;

/**
 * @author Niwer
 * @author noz43
 */
public class ClientRequestNetworkConfig implements IPacket {
    
    public ClientRequestNetworkConfig() {
    }

    @Override
    public void handle(Connection connection) {
        ServerResponseNetworkConfig response = new ServerResponseNetworkConfig(NetworkDirectories.getConfig());
        connection.sendTCP(response);
    }
}
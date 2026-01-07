package com.photon.network.messages.requests.server;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.NetworkEngine;
import com.photon.network.messages.response.ServerResponseServerList;

/**
 * @author Niwer
 * @author noz43
 */

public class ClientRequestServerList implements IPacket {
    
    @Override
    public void handle(Connection connection) {
        ServerResponseServerList response = new ServerResponseServerList(NetworkEngine.SAVED_SERVER_LIST);
        connection.sendTCP(response);
    }
}
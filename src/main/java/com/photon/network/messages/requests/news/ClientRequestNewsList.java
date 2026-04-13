package com.photon.network.messages.requests.news;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.NetworkEngine;
import com.photon.network.messages.response.ServerResponseNewsList;

/**
 * @author Niwer
 * @author noz43
 */

public class ClientRequestNewsList implements IPacket {
    
    public ClientRequestNewsList() {}

    @Override
    public void handle(Connection connection) {
        ServerResponseNewsList response = new ServerResponseNewsList(NetworkEngine.SAVED_NEWS_LIST);
        connection.sendTCP(response);
    }
}
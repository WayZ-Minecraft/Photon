package com.photon.network.messages.requests.news;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.NetworkEngine;
import com.photon.network.messages.response.ServerResponseNewsList;

/**
 * @author noz43
 */

public class ClientRequestNewsList {
    
    public void handle(Connection connection) {
        ServerResponseNewsList response = new ServerResponseNewsList(NetworkEngine.SAVED_NEWS_LIST);
        connection.sendTCP(response);
    }
}
package com.photon.network.messages.response;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonClientData;
import com.photon.network.IPacket;
import com.photon.network.objects.ObjectNews;

/**
 * @author Niwer
 * @author noz43
 */
public class ServerResponseNewsList implements IPacket {
    private final List<ObjectNews> newsObjects;
    
    public ServerResponseNewsList() {
        this.newsObjects = new ArrayList<>();
    }

    public ServerResponseNewsList(List<ObjectNews> newsObjects) {
        this.newsObjects = new ArrayList<>(newsObjects);
    }
    
    public List<ObjectNews> getNewsObjects() { return new ArrayList<>(newsObjects); }
    
    @Override
    public void handle(Connection connection) {
        PhotonClientData.CLIENT_NEWS_LIST.clear();
        PhotonClientData.CLIENT_NEWS_LIST.addAll(newsObjects);
    }
}
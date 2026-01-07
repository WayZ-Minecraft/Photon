package com.photon.network.messages.response;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.objects.ObjectNews;

/**
 * @author noz43
 */

public class ServerResponseNewsList {
    private final List<ObjectNews> newsObjects;
    
    public ServerResponseNewsList(List<ObjectNews> newsObjects) {
        this.newsObjects = new ArrayList<>(newsObjects);
    }
    
    public ArrayList<ObjectNews> getNewsObjects() { return new ArrayList<>(newsObjects); }
    
    public void handle(Connection connection) {
        PhotonEngine.clientNewsList = new ArrayList<>(newsObjects);
        synchronized (PhotonEngine.clientNewsListWaiter) {
            PhotonEngine.clientNewsListWaiter.notify();
        }
    }
}
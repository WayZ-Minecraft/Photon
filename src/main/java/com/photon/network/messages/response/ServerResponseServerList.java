package com.photon.network.messages.response;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.objects.ObjectServer;

/**
 * @author noz43
 */

public class ServerResponseServerList {
    private final List<ObjectServer> serverObjects;
    
    public ServerResponseServerList(List<ObjectServer> serverObjects) {
        this.serverObjects = new ArrayList<>(serverObjects);
    }
    
    public ArrayList<ObjectServer> getServerObjects() { return new ArrayList<>(serverObjects); }
    
    public void handle(Connection connection) {
        PhotonEngine.clientServerList = new ArrayList<>(serverObjects);
        synchronized (PhotonEngine.clientServerListWaiter) {
            PhotonEngine.clientServerListWaiter.notify();
        }
    }
}
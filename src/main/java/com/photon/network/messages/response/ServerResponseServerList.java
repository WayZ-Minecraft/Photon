package com.photon.network.messages.response;

import java.util.ArrayList;
import java.util.List;

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
}
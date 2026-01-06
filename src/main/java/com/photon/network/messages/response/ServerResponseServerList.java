package com.photon.network.messages.response;

import java.util.ArrayList;
import com.photon.network.objects.ObjectServer;

/**
 * @author noz43
 */
public class ServerResponseServerList {
    private final ArrayList<ObjectServer> serverObjects;
    
    public ServerResponseServerList(ArrayList<ObjectServer> serverObjects) {
        this.serverObjects = new ArrayList<>(serverObjects);
    }
    
    public ArrayList<ObjectServer> getServerObjects() { return new ArrayList<>(serverObjects); }
}
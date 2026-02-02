package com.photon.network.messages.response;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonClientData;
import com.photon.network.IPacket;
import com.photon.network.objects.ObjectServer;

/**
 * @author Niwer
 * @author noz43
 */
public class ServerResponseServerList implements IPacket {
    private final List<ObjectServer> serverObjects;
    
    public ServerResponseServerList() {
        this.serverObjects = new ArrayList<>();
    }

    public ServerResponseServerList(List<ObjectServer> serverObjects) {
        this.serverObjects = new ArrayList<>(serverObjects);
    }
    
    public List<ObjectServer> getServerObjects() { return new ArrayList<>(serverObjects); }
    
    @Override
    public void handle(Connection connection) {
        PhotonClientData.CLIENT_SERVER_LIST.clear();
        PhotonClientData.CLIENT_SERVER_LIST.addAll(serverObjects);
    }
}
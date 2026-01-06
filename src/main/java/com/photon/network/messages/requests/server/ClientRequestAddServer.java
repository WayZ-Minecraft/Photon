package com.photon.network.messages.requests.server;

import com.photon.network.objects.ObjectServer;

/**
 * @author noz43
 */
public class ClientRequestAddServer {
    private final ObjectServer objServer;
    
    public ClientRequestAddServer(ObjectServer objServer) {
        this.objServer = objServer;
    }
    
    public ObjectServer getObjServer() { return objServer; }
}
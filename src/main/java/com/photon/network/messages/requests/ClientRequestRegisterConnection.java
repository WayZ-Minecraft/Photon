package com.photon.network.messages.requests;

/**
 * @author noz43
 */
public class ClientRequestRegisterConnection {
    private final String uuid;
    
    public ClientRequestRegisterConnection(String uuid) {
        this.uuid = uuid;
    }
    
    public String getUuid() { return uuid; }
}
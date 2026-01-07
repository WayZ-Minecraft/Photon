package com.photon.network.messages.response.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.objects.ObjectPlayerAccount;

/**
 * @author noz43
 */

public class ServerResponseAccount {
    private final ObjectPlayerAccount givenProfile;
    
    public ServerResponseAccount(ObjectPlayerAccount givenProfile) {
        this.givenProfile = givenProfile;
    }
    
    public ObjectPlayerAccount getGivenProfile() { return givenProfile; }
    
    public void handle(Connection connection) {
        PhotonEngine.clientPlayerProfile = givenProfile;
        synchronized (PhotonEngine.clientPlayerProfileWaiter) {
            PhotonEngine.clientPlayerProfileWaiter.notify();
        }
    }
}
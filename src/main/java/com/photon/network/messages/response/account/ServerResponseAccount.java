package com.photon.network.messages.response.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.IPacket;
import com.photon.network.objects.ObjectPlayerAccount;

/**
 * @author Niwer
 * @author noz43
 */
public class ServerResponseAccount implements IPacket {
    private final ObjectPlayerAccount givenProfile;
    
    public ServerResponseAccount() {
        this.givenProfile = null;
    }

    public ServerResponseAccount(ObjectPlayerAccount givenProfile) {
        this.givenProfile = givenProfile;
    }
    
    public ObjectPlayerAccount getGivenProfile() { return givenProfile; }
    
    @Override
    public void handle(Connection connection) {
        PhotonEngine.clientPlayerProfile = givenProfile;
        synchronized (PhotonEngine.clientPlayerProfileWaiter) {
            PhotonEngine.clientPlayerProfileWaiter.notify();
        }
    }
}
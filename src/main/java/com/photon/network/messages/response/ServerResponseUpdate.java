package com.photon.network.messages.response;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonClientData;
import com.photon.network.IPacket;

/**
 * @author Niwer
 * @author noz43
 */
public class ServerResponseUpdate implements IPacket {
    private final byte[] data;
    private final String sha1;
    
    public ServerResponseUpdate() {
        this.data = new byte[0];
        this.sha1 = "";
    }

    public ServerResponseUpdate(byte[] data, String sha1) {
        this.data = data;
        this.sha1 = sha1;
    }
        
    @Override
    public void handle(Connection connection) {
        PhotonClientData.UPDATE_DATA.set(data);
        PhotonClientData.UPDATE_SHA.set(sha1);
    }
}
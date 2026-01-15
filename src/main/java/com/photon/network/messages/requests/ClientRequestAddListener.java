package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.listeners.INetworkMessageListener;
import com.photon.network.listeners.MessageListenerCommon;

/**
 * @author Niwer
 * @author noz43
 */

public class ClientRequestAddListener implements IPacket {
    private final INetworkMessageListener listener;
    
    public ClientRequestAddListener() {
        this.listener = null;
    }

    public ClientRequestAddListener(INetworkMessageListener listener) {
        this.listener = listener;
    }
    
    public INetworkMessageListener getListener() { return listener; }
    
    @Override
    public void handle(Connection connection) {
        MessageListenerCommon.addListener(listener);
    }
}
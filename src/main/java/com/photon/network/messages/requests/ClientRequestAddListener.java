package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.listeners.INetworkMessageListener;
import com.photon.network.listeners.MessageListenerCommon;

/**
 * @author noz43
 */

public class ClientRequestAddListener {
    private final INetworkMessageListener listener;
    
    public ClientRequestAddListener(INetworkMessageListener listener) {
        this.listener = listener;
    }
    
    public INetworkMessageListener getListener() { return listener; }
    
    public void handle(Connection connection) {
        MessageListenerCommon.addListener(listener);
    }
}
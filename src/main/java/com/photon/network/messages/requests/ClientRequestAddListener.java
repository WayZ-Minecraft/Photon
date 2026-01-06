package com.photon.network.messages.requests;

import com.photon.network.listeners.INetworkMessageListener;

/**
 * @author noz43
 */
public class ClientRequestAddListener {
    private final INetworkMessageListener listener;
    
    public ClientRequestAddListener(INetworkMessageListener listener) {
        this.listener = listener;
    }
    
    public INetworkMessageListener getListener() { return listener; }
}
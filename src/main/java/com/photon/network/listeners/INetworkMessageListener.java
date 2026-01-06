package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;

public interface INetworkMessageListener {

    INetworkListenerSide applyTo();
    
    INetworkListenerSide useOn();
    
    void received(Connection connection, Object object);
    
    default int getPriority() {
        return 0;
    }
    
    default boolean canHandle(Class<?> messageClass) {
        return true;
    }
    
    enum INetworkListenerSide {
        CLIENT, SERVER
    }
}
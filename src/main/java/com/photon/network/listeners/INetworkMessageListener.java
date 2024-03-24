package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;

/**
 * Interface to add a network message listeners
 */
public interface INetworkMessageListener {

    /**
     * Will be use to determine if the listener is for the client or the server
     * @return The side of the listener
     */
	public INetworkListenerSide applyTo();
	
	public INetworkListenerSide useOn();
	
    /**
     * Will be called when a message is received
     * @param connection The connection
     * @param object The object received
     */
    public void received(final Connection connection, final Object object);
    
    public static enum INetworkListenerSide { CLIENT, SERVER }
}

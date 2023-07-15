package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;

public interface INetworkMessageListener {

	public INetworkListenerSide applyTo();
	
	public INetworkListenerSide useOn();
	
    public void received(final Connection connection, final Object object);
    
    public static enum INetworkListenerSide { CLIENT, SERVER }
}

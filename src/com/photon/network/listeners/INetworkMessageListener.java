package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;

public interface INetworkMessageListener {

	public boolean serverSide();
	
    public void received(final Connection connection, final Object object);
}

package com.photon.network.listeners;

import java.util.ArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.network.messages.requests.ClientRequestAddListener;

public class MessageListenerCommon implements Listener
{
	public static ArrayList<INetworkMessageListener> listeners = new ArrayList<>();
	
    @Override
    public void disconnected(final Connection connection) {
        NetworkConnectionClient.attemptReconnectionFromClient();
    }
    
    /**
     * Notify an object as received
     * @param object The object to notify
     */
    public static void notifyObjectAsReceived(Object object) {
        synchronized (object) {
            object.notify();
        }
    }

    /**
     * Register a listener to the network
     * @param listener The listener to register
     */
    public static void registerListener(INetworkMessageListener listener) {
    	if(listener.applyTo() == INetworkListenerSide.CLIENT) addListener(listener);
    	else {
    		final ClientRequestAddListener packet = new ClientRequestAddListener();
    		packet.listener = listener;
    		NetworkConnectionClient.sendTCP(packet);
    	}
    }
    
    protected static void addListener(INetworkMessageListener listener) {
    	if(!listeners.contains(listener)) listeners.add(listener);
    }
}

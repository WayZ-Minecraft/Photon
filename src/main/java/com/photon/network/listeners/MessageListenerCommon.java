package com.photon.network.listeners;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.network.messages.requests.ClientRequestAddListener;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class MessageListenerCommon implements Listener {
    
    public static final CopyOnWriteArrayList<INetworkMessageListener> listeners = new CopyOnWriteArrayList<>();
    
    private static boolean listenersSorted = false;
    
    @Override
    public void disconnected(Connection connection) {
        ConsoleManager.create("Connection lost, attempting reconnection...")
            .withType(EnumLogType.NETWORK)
            .end();
        NetworkConnectionClient.attemptReconnectionFromClient();
    }
    
    public static void notifyObjectAsReceived(Object object) {
        synchronized (object) {
            object.notify();
        }
    }

    public static void registerListener(INetworkMessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        
        if (listener.applyTo() == INetworkListenerSide.CLIENT) {
            addListener(listener);
        } else {
            ClientRequestAddListener packet = new ClientRequestAddListener(listener);
            NetworkConnectionClient.sendTCP(packet);
        }
    }
    
    protected static void addListener(INetworkMessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            listenersSorted = false;
            
            ConsoleManager.create("Registered listener: " + listener.getClass().getSimpleName())
                .withType(EnumLogType.NETWORK)
                .end();
        }
    }
    
    public static boolean removeListener(INetworkMessageListener listener) {
        boolean removed = listeners.remove(listener);
        
        if (removed) {
            ConsoleManager.create("Unregistered listener: " + listener.getClass().getSimpleName())
                .withType(EnumLogType.NETWORK)
                .end();
        }
        
        return removed;
    }
    
    public static void dispatchToListeners(Connection connection, Object object, INetworkListenerSide side) {
        ensureListenersSorted();
        
        Class<?> messageClass = object.getClass();
        
        for (INetworkMessageListener listener : listeners) {
            if (listener.useOn() == side && listener.canHandle(messageClass)) {
                try {
                    listener.received(connection, object);
                } catch (Exception e) {
                    ConsoleManager.create("Error in listener " + listener.getClass().getSimpleName() + 
                        " while handling " + messageClass.getSimpleName())
                        .withType(EnumLogType.NETWORK)
                        .error()
                        .end();
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void ensureListenersSorted() {
        if (!listenersSorted) {
            listeners.sort(Comparator.comparingInt(INetworkMessageListener::getPriority).reversed());
            listenersSorted = true;
        }
    }
    
    public static void clearAllListeners() {
        listeners.clear();
        listenersSorted = false;
        
        ConsoleManager.create("Cleared all network listeners")
            .withType(EnumLogType.NETWORK)
            .end();
    }
    
    public static int getListenerCount() {
        return listeners.size();
    }
}
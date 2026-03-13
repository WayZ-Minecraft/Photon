package com.photon.network.listeners;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.network.ClientLinkManager;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.network.messages.requests.ClientRequestAddListener;
import com.photon.util.PhotonLogTypes;

import niwer.lumen.Console;

/**
 * TODO this will have to be rework to make support for Content-Packs download.
 * But not required immediately.
 */
public class MessageListenerCommon implements Listener {
    
    public static final List<INetworkMessageListener> REGISTERED_LISTENERS = new CopyOnWriteArrayList<>();
    
    private static boolean listenersSorted = false;

    public static void notifyObjectAsReceived(Object object) {
        synchronized (object) { object.notify(); }
    }

    public static void registerListener(INetworkMessageListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        
        if (listener.applyTo() == INetworkListenerSide.CLIENT) addListener(listener);
        else {
            ClientRequestAddListener packet = new ClientRequestAddListener(listener);
            ClientLinkManager.sendTCP(packet);
        }
    }
    
    public static void addListener(INetworkMessageListener listener) {
        if (!REGISTERED_LISTENERS.contains(listener)) {
            REGISTERED_LISTENERS.add(listener);
            listenersSorted = false;
            Console.log("Registered listener: " + listener.getClass().getSimpleName()).type(PhotonLogTypes.NETWORK).send();
        }
    }
    
    public static boolean removeListener(INetworkMessageListener listener) {
        final boolean REMOVAL_SUCCESS = REGISTERED_LISTENERS.remove(listener);
        if (!REMOVAL_SUCCESS) Console.log("Unregistered listener: " + listener.getClass().getSimpleName()).type(PhotonLogTypes.NETWORK).send();
        return REMOVAL_SUCCESS;
    }
    
    public static void dispatchToListeners(Connection connection, Object object, INetworkListenerSide side) {
        ensureListenersSorted();
        
        Class<?> messageClass = object.getClass();
        
        for (INetworkMessageListener listener : REGISTERED_LISTENERS) {
            if (listener.useOn() == side && listener.canHandle(messageClass)) {
                try {
                    listener.received(connection, object);
                } catch (Exception e) {
                    Console.log("Error in listener " + listener.getClass().getSimpleName() + 
                        " while handling " + messageClass.getSimpleName())
                        .type(PhotonLogTypes.NETWORK)
                        .error()
                        .send();
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void ensureListenersSorted() {
        if (!listenersSorted) {
            REGISTERED_LISTENERS.sort(Comparator.comparingInt(INetworkMessageListener::getPriority).reversed());
            listenersSorted = true;
        }
    }
    
    public static void clearAllListeners() {
        REGISTERED_LISTENERS.clear();
        listenersSorted = false;
        
        Console.log("Cleared all network listeners").type(PhotonLogTypes.NETWORK).send();
    }
}
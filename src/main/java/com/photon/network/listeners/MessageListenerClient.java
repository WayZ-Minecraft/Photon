package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.network.ClientLinkManager;
import com.photon.network.IPacket;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class MessageListenerClient implements Listener {
    
    @Override
    public void received(Connection connection, Object object) {
        try {
            if (object instanceof IPacket packet) packet.handle(connection);
            MessageListenerCommon.dispatchToListeners(connection, object, INetworkListenerSide.CLIENT);
        } catch (Exception e) {
            ConsoleManager.create("Unexpected error while handling: " + object.getClass().getSimpleName())
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            e.printStackTrace();
        }
    }

     @Override
    public void disconnected(Connection connection) {
        ConsoleManager.create("Connection lost, attempting reconnection...").withType(EnumLogType.NETWORK).end();
        ClientLinkManager.attemptReconnectionFromClient();
    }
}
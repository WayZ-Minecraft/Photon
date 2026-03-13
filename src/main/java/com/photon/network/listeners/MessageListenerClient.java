package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.network.ClientLinkManager;
import com.photon.network.IPacket;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.util.PhotonLogTypes;

import niwer.lumen.Console;

public class MessageListenerClient implements Listener {
    
    @Override
    public void received(Connection connection, Object object) {
        try {
            if (object instanceof IPacket packet) packet.handle(connection);
            MessageListenerCommon.dispatchToListeners(connection, object, INetworkListenerSide.CLIENT);
        } catch (Exception e) {
            Console.log("Unexpected error while handling: " + object.getClass().getSimpleName())
                .type(PhotonLogTypes.NETWORK)
                .error()
                .send();
            e.printStackTrace();
        }
    }

     @Override
    public void disconnected(Connection connection) {
        Console.log("Connection lost, attempting reconnection...").type(PhotonLogTypes.NETWORK).send();
        ClientLinkManager.attemptReconnectionFromClient();
    }
}
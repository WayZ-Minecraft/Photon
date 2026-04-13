package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.Listener;
import com.photon.PhotonEngine;
import com.photon.network.IPacket;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.util.NetworkOnly;
import com.photon.util.PhotonLogTypes;

import niwer.lumen.Console;

@NetworkOnly
public class MessageListenerServer implements Listener {
    
    @Override
    public void received(Connection connection, Object object) {
        try {
            if(!(object instanceof KeepAlive))
                Console.log("Received packet: " + object.getClass().getSimpleName()).type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
            
            if (object instanceof IPacket packet) packet.handle(connection);
            MessageListenerCommon.dispatchToListeners(connection, object, INetworkListenerSide.SERVER);
        } catch (Exception e) {
            Console.log("Unexpected error while handling: " + object.getClass().getSimpleName())
                .type(PhotonLogTypes.NETWORK)
                .error()
                .send();
            e.printStackTrace();
        }
    }
}
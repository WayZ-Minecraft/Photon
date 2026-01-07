package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.network.messages.response.ServerResponseNetworkConfig;
import com.photon.network.messages.response.ServerResponseNewsList;
import com.photon.network.messages.response.ServerResponseServerList;
import com.photon.network.messages.response.account.ServerResponseAccount;
import com.photon.network.messages.response.account.ServerResponseValidAccount;

public class MessageListenerClient implements Listener {
    
    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ServerResponseAccount packet) {
            packet.handle(connection);
        }
        else if (object instanceof ServerResponseValidAccount packet) {
            packet.handle(connection);
        }
        else if (object instanceof ServerResponseNewsList packet) {
            packet.handle(connection);
        }
        else if (object instanceof ServerResponseServerList packet) {
            packet.handle(connection);
        }
        else if (object instanceof ServerResponseNetworkConfig packet) {
            packet.handle(connection);
        }
        
        MessageListenerCommon.dispatchToListeners(connection, object, INetworkListenerSide.CLIENT);
    }
}
package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.Listener;
import com.photon.PhotonEngine;
import com.photon.network.NetworkDirectories;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.network.messages.response.ServerResponseNetworkConfig;
import com.photon.network.messages.response.ServerResponseNewsList;
import com.photon.network.messages.response.ServerResponseServerList;
import com.photon.network.messages.response.account.ServerResponseAccount;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class MessageListenerClient implements Listener
{	
    @Override
    public void received(final Connection connection, final Object object) {
        if(!(object instanceof KeepAlive)) ConsoleManager.create("Received object from server : " + object.getClass().getSimpleName()).withType(EnumLogType.NETWORK).end();
        if (object instanceof ServerResponseAccount) {
            PhotonEngine.clientPlayerProfile = ((ServerResponseAccount)object).givenProfile;
            MessageListenerCommon.notifyObjectAsReceived(PhotonEngine.clientPlayerProfileWaiter);
        } else if (object instanceof ServerResponseValidAccount) {
            PhotonEngine.clientAccountResponse = (ServerResponseValidAccount)object;
            MessageListenerCommon.notifyObjectAsReceived(PhotonEngine.clientAccountResponseWaiter);
        } else if (object instanceof ServerResponseNewsList) {
            PhotonEngine.clientNewsList = ((ServerResponseNewsList)object).newsObjects;
            MessageListenerCommon.notifyObjectAsReceived(PhotonEngine.clientNewsListWaiter);
        } else if (object instanceof ServerResponseServerList) {
            PhotonEngine.clientServerList = ((ServerResponseServerList)object).serverObjects;
            MessageListenerCommon.notifyObjectAsReceived(PhotonEngine.clientServerListWaiter);
        } else if (object instanceof ServerResponseNetworkConfig) {
            NetworkDirectories.config = ((ServerResponseNetworkConfig)object).config;
            MessageListenerCommon.notifyObjectAsReceived(NetworkDirectories.configWaiter);
        }

        /* Send to custom listeners */
        for(INetworkMessageListener listener : MessageListenerCommon.listeners) {
        	if(listener.useOn() == INetworkListenerSide.CLIENT) listener.received(connection, object);
        }
    }
}

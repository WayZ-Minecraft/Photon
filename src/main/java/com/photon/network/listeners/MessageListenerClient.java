package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.PhotonEngine;
import com.photon.network.NetworkDirectories;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.network.messages.response.ServerResponseNetworkConfig;
import com.photon.network.messages.response.ServerResponseNewsList;
import com.photon.network.messages.response.ServerResponseServerList;
import com.photon.network.messages.response.account.ServerResponseAccount;
import com.photon.network.messages.response.account.ServerResponseValidAccount;

public class MessageListenerClient implements Listener
{	
    @Override
    public void received(final Connection connection, final Object object) {
        if (object instanceof ServerResponseAccount) PhotonEngine.clientPlayerProfile = ((ServerResponseAccount)object).givenProfile;
        else if (object instanceof ServerResponseNewsList) PhotonEngine.clientNewsList = ((ServerResponseNewsList)object).newsObjects;
        else if (object instanceof ServerResponseServerList) PhotonEngine.clientServerList = ((ServerResponseServerList)object).serverObjects;
        else if (object instanceof ServerResponseValidAccount) PhotonEngine.clientAccountResponse = (ServerResponseValidAccount)object;
        else if (object instanceof ServerResponseNetworkConfig) NetworkDirectories.config = ((ServerResponseNetworkConfig)object).config;
        for(INetworkMessageListener listener : MessageListenerCommon.listeners) {
        	if(listener.useOn() == INetworkListenerSide.CLIENT) {
        		listener.received(connection, object);
        	}
        }
    }
}

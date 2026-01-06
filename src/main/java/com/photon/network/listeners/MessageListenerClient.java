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

public class MessageListenerClient implements Listener {
    
    @Override
    public void received(Connection connection, Object object) {
        handleCoreResponses(object);
        MessageListenerCommon.dispatchToListeners(connection, object, INetworkListenerSide.CLIENT);
    }
    
    private void handleCoreResponses(Object object) {
        if (object instanceof ServerResponseAccount response) {
            PhotonEngine.clientPlayerProfile = response.getGivenProfile();
            MessageListenerCommon.notifyObjectAsReceived(PhotonEngine.clientPlayerProfileWaiter);
        } 
        else if (object instanceof ServerResponseValidAccount response) {
            PhotonEngine.clientAccountResponse = response;
            MessageListenerCommon.notifyObjectAsReceived(PhotonEngine.clientAccountResponseWaiter);
        } 
        else if (object instanceof ServerResponseNewsList response) {
            PhotonEngine.clientNewsList = response.getNewsObjects();
            MessageListenerCommon.notifyObjectAsReceived(PhotonEngine.clientNewsListWaiter);
        } 
        else if (object instanceof ServerResponseServerList response) {
            PhotonEngine.clientServerList = response.getServerObjects();
            MessageListenerCommon.notifyObjectAsReceived(PhotonEngine.clientServerListWaiter);
        } 
        else if (object instanceof ServerResponseNetworkConfig response) {
            NetworkDirectories.config = response.getConfig();
            MessageListenerCommon.notifyObjectAsReceived(NetworkDirectories.configWaiter);
        }
    }
}
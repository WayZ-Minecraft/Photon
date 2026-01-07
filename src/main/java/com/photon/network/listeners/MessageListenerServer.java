package com.photon.network.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.network.NetworkLinkManager;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.network.messages.requests.ClientRequestAddClass;
import com.photon.network.messages.requests.ClientRequestAddListener;
import com.photon.network.messages.requests.ClientRequestAnticheat;
import com.photon.network.messages.requests.ClientRequestCrashReport;
import com.photon.network.messages.requests.ClientRequestHWID;
import com.photon.network.messages.requests.ClientRequestNetworkConfig;
import com.photon.network.messages.requests.ClientRequestRegisterConnection;
import com.photon.network.messages.requests.ClientRequestSendDiscordLogs;
import com.photon.network.messages.requests.ClientRequestSyncContentPacks;
import com.photon.network.messages.requests.account.ClientRequestAccount;
import com.photon.network.messages.requests.account.ClientRequestAccountCreation;
import com.photon.network.messages.requests.account.ClientRequestAccountVerification;
import com.photon.network.messages.requests.news.ClientRequestNewsList;
import com.photon.network.messages.requests.server.ClientRequestAddServer;
import com.photon.network.messages.requests.server.ClientRequestServerList;
import com.photon.network.objects.ObjectContentPack;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class MessageListenerServer implements Listener {
    
    @Override
    public void received(Connection connection, Object object) {
        try {
            if (object instanceof ClientRequestRegisterConnection packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestAccount packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestAccountVerification packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestAccountCreation packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestSyncContentPacks packet) {
                packet.handle(connection);
            }
            else if (object instanceof ObjectContentPack pack) {
                NetworkLinkManager.SERVER.sendToTCP(pack.connectionID(), pack);
            }
            else if (object instanceof ClientRequestCrashReport packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestAnticheat packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestHWID packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestNewsList packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestSendDiscordLogs packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestAddServer packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestServerList packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestNetworkConfig packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestAddClass packet) {
                packet.handle(connection);
            }
            else if (object instanceof ClientRequestAddListener packet) {
                packet.handle(connection);
            }
            
            MessageListenerCommon.dispatchToListeners(connection, object, INetworkListenerSide.SERVER);
        } catch (Exception e) {
            ConsoleManager.create("Unexpected error while handling: " + object.getClass().getSimpleName())
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            e.printStackTrace();
        }
    }
}
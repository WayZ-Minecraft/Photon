package com.photon.network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.photon.PhotonEngine;
import com.photon.network.NetworkDirectories.NetworkConfig;
import com.photon.network.listeners.MessageListenerClient;
import com.photon.network.listeners.MessageListenerCommon;
import com.photon.network.messages.requests.ClientRequestNetworkConfig;
import com.photon.network.messages.requests.ClientRequestRegisterConnection;

public class NetworkConnectionClient
{
    public static Client client;
        
    public static void load() throws IOException {
    	client = new Client(NetworkConfig.writeBufferSize, NetworkConfig.objectBufferSize);
    	client.start();
    	client.connect(5000, PhotonEngine.network_Ip, PhotonEngine.network_Tcp, PhotonEngine.network_Udp);
    	client.addListener(new MessageListenerClient());
    	client.addListener(new MessageListenerCommon());
    	NetworkObjectRegistry.load(client.getKryo());
    	NetworkConnectionClient.client.sendTCP(new ClientRequestRegisterConnection());
    	NetworkConnectionClient.client.sendTCP(new ClientRequestNetworkConfig());
    	while(NetworkDirectories.config == null) {}
    }
    
    public static void attemptReconnectionFromClient() {
        new Thread() {
            @Override
            public void run() {
                try {
                    client.stop();
                    client.start();
                    client.reconnect();
                } catch (IOException ex) { ex.printStackTrace(); }
            }
        }.start();
    }
}

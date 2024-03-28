package com.photon.network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.photon.PhotonEngine;
import com.photon.network.NetworkDirectories.NetworkConfig;
import com.photon.network.listeners.MessageListenerClient;
import com.photon.network.listeners.MessageListenerCommon;
import com.photon.network.messages.requests.ClientRequestNetworkConfig;
import com.photon.network.messages.requests.ClientRequestRegisterConnection;
import com.photon.util.ConsoleManager;
import com.photon.util.ProtectorManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class NetworkConnectionClient {
    protected static Client client = new Client(NetworkConfig.writeBufferSize, NetworkConfig.objectBufferSize);
    
    public static void load() throws IOException {
        NetworkObjectRegistry.load(client.getKryo());
    	new Thread(client).start();
    	client.setKeepAliveTCP(ProtectorManager.TIME_OUT);
        client.connect(5000, PhotonEngine.network_Ip, PhotonEngine.network_Tcp, PhotonEngine.network_Udp);
    	client.addListener(new MessageListenerClient());
    	client.addListener(new MessageListenerCommon());
        try {
            if(NetworkDirectories.config.isEmpty()) {
                NetworkConnectionClient.sendTCP(new ClientRequestNetworkConfig());
                Thread.sleep(ProtectorManager.TIME_OUT);
            }
        } catch (InterruptedException e) { ConsoleManager.create(ConsoleManager.of(e)).withType(EnumLogType.NETWORK).error().end(); }
        NetworkConnectionClient.sendTCP(new ClientRequestRegisterConnection());
    }
    
    public static boolean isConnected() { return client !=null && client.isConnected(); }

    public static void sendTCP(Object obj) {
        if(!isConnected()) return;
        ConsoleManager.create("Sending : "+obj).withType(EnumLogType.NETWORK).end();
        client.sendTCP(obj);
    }
    
    public static void sendUDP(Object obj) {
        if(!isConnected()) return;
        client.sendUDP(obj);
    }

    public static void attemptReconnectionFromClient() {
        ConsoleManager.debug("Disconnected from server, attempting reconnection...");
        new Thread(() -> {
            while(!isConnected()) {
                try {
                    client.stop();
                    client.start();
                    client.reconnect();
                } catch (IOException ex) {}
            }
        }, "Network-disconnected-thread").start();
    }
}

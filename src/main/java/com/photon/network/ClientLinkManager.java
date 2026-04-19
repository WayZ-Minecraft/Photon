package com.photon.network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.photon.Directories;
import com.photon.PhotonEngine;
import com.photon.Directories.NetworkConfig;
import com.photon.network.listeners.MessageListenerClient;
import com.photon.network.listeners.MessageListenerCommon;
import com.photon.network.messages.requests.ClientRequestRegisterConnection;
import com.photon.network.http.NetworkWebClient;
import com.photon.util.PhotonLogTypes;
import com.photon.util.ProtectorManager;

import niwer.lumen.Console;

/**
 * Use in mod/launchers/external applications to connect to Photon servers.
 */
public class ClientLinkManager {
    protected static Client client = new Client(NetworkConfig.WRITE_BUFFER_SIZE, NetworkConfig.OBJECT_BUFFER_SIZE);
    
    public static void load() throws IOException {
        try {
            if(Directories.getConfig() == null || Directories.getConfig().isEmpty()) {
                final NetworkConfig fetchedConfig = NetworkWebClient.fetchNetworkConfig(PhotonEngine.network_Ip, Directories.getConfig().webserver_port);
                if (fetchedConfig != null) {
                    Directories.config = fetchedConfig;
                    Directories.save();
                }
            }
        } catch (Exception e) { Console.log(e).type(PhotonLogTypes.NETWORK).error().send(); }
        NetworkObjectRegistry.load(client.getKryo());
	new Thread(client, "Client Network Connection").start();
        client.connect(5000, PhotonEngine.network_Ip, PhotonEngine.network_Tcp, PhotonEngine.network_Udp);
	client.addListener(new MessageListenerClient());
	client.addListener(new MessageListenerCommon());
        ClientLinkManager.sendTCP(new ClientRequestRegisterConnection(ProtectorManager.getHWID()));
    }
    
    public static boolean isConnected() { return client !=null && client.isConnected(); }

    public static void sendTCP(Object obj) {
        if(!isConnected()) return;
        client.sendTCP(obj);
    }
    
    public static void sendUDP(Object obj) {
        if(!isConnected()) return;
        client.sendUDP(obj);
    }

    public static void refreshServerListFromWeb() throws IOException {
        NetworkWebClient.refreshServerList(PhotonEngine.network_Ip, Directories.getConfig().webserver_port);
    }
    
    public static void attemptReconnectionFromClient() {
        Console.log("Disconnected from server, attempting reconnection...").error().type(PhotonLogTypes.NETWORK).send();
        Thread t = new Thread(() -> {
            while(!isConnected()) {
                try {
                    client.stop();
                    client.start();
                    client.reconnect();
                } catch (IOException ex) {}
            }
        }, "Network-disconnected-thread");
        t.setDaemon(true);
        t.start();
    }
}
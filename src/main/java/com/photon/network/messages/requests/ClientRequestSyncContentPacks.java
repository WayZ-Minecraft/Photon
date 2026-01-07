package com.photon.network.messages.requests;

import java.util.HashMap;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.NetworkEngine;
import com.photon.network.NetworkLinkManager;
import com.photon.network.messages.response.ServerResponseSyncContentPack;
import com.photon.network.objects.ObjectServer;

/**
 * @author Niwer
 * @author noz43
 */
public class ClientRequestSyncContentPacks implements IPacket {
    private final String ip;
    private final int port;
    private final int filesCount;
    private final HashMap<String, String> sha1;
    
    public ClientRequestSyncContentPacks(String ip, int port, int filesCount, HashMap<String, String> sha1) {
        this.ip = ip;
        this.port = port;
        this.filesCount = filesCount;
        this.sha1 = new HashMap<>(sha1);
    }
    
    public String getIp() { return ip; }
    public int getPort() { return port; }
    public int getFilesCount() { return filesCount; }
    public HashMap<String, String> getSha1() { return new HashMap<>(sha1); }
    
    @Override
    public void handle(Connection connection) {
        for (ObjectServer server : NetworkEngine.SAVED_SERVER_LIST) {
            if (server.serverIP.equalsIgnoreCase(ip) && server.serverPort == port) {
                ServerResponseSyncContentPack response = new ServerResponseSyncContentPack(
                    server.connectionID,
                    filesCount,
                    sha1
                );
                
                NetworkLinkManager.SERVER.sendToTCP(server.connectionID, response);
                break;
            }
        }
    }
}
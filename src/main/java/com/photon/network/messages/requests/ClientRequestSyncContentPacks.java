package com.photon.network.messages.requests;

import java.util.HashMap;

/**
 * @author noz43
 */
public class ClientRequestSyncContentPacks {
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
}
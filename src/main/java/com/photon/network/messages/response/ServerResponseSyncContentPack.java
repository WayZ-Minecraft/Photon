package com.photon.network.messages.response;

import java.util.HashMap;

/**
 * @author noz43
 */
public class ServerResponseSyncContentPack {
    private final int connectionID;
    private final int filesCount;
    private final HashMap<String, String> sha1;
    
    public ServerResponseSyncContentPack(int connectionID, int filesCount, HashMap<String, String> sha1) {
        this.connectionID = connectionID;
        this.filesCount = filesCount;
        this.sha1 = new HashMap<>(sha1);
    }
    
    public int getConnectionID() { return connectionID; }
    public int getFilesCount() { return filesCount; }
    public HashMap<String, String> getSha1() { return new HashMap<>(sha1); }
}
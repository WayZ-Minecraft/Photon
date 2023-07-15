package com.photon.network.messages.requests;

import java.util.HashMap;

public class ClientRequestSyncContentPacks {
    public String ip;
    public int port;
    public int filesCount;
    public HashMap<String, String> sha1 = new HashMap<>();
}

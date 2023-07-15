package com.photon.network.messages.response;

import java.util.HashMap;

public class ServerResponseSyncContentPack {
	public int connectionID;
	public int filesCount = -1;
	public HashMap<String, String> sha1 = new HashMap<>();
}

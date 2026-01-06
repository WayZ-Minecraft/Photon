package com.photon.network.messages.response;

import com.photon.network.NetworkDirectories.NetworkConfig;

/**
 * @author noz43
 */
public class ServerResponseNetworkConfig {
    private final NetworkConfig config;
    
    public ServerResponseNetworkConfig(NetworkConfig config) {
        this.config = config;
    }
    
    public NetworkConfig getConfig() { return config; }
}
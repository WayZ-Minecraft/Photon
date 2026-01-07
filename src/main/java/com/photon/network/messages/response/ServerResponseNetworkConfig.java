package com.photon.network.messages.response;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.NetworkDirectories;
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
    
    public void handle(Connection connection) {
        NetworkDirectories.config = config;
        synchronized (NetworkDirectories.configWaiter) {
            NetworkDirectories.configWaiter.notify();
        }
    }
}
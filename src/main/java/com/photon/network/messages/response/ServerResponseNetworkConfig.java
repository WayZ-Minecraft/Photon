package com.photon.network.messages.response;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.NetworkDirectories;
import com.photon.network.NetworkDirectories.NetworkConfig;

/**
 * @author Niwer
 * @author noz43
 */
public class ServerResponseNetworkConfig implements IPacket {
    private final NetworkConfig config;
    
    public ServerResponseNetworkConfig() {
        this.config = null;
    }

    public ServerResponseNetworkConfig(NetworkConfig config) {
        this.config = config;
    }
    
    public NetworkConfig getConfig() { return config; }
    
    @Override
    public void handle(Connection connection) {
        NetworkDirectories.config = config;
        synchronized (NetworkDirectories.configWaiter) {
            NetworkDirectories.configWaiter.notify();
        }
    }
}
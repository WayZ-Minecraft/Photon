package com.photon.network;

import com.esotericsoftware.kryonet.Server;
import com.photon.PhotonEngine;
import com.photon.network.NetworkDirectories.NetworkConfig;
import com.photon.network.listeners.MessageListenerServer;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.NetworkOnly;
import com.photon.util.os.ApplicationUtils;

/**
 * This class manager the kryonet server (network link) for Photon Engine
 * @author Niwer
 */
@NetworkOnly
public class NetworkLinkManager
{
    public static final long TIME_BEFORE_STOP = 2500L;
    public static final Server SERVER = new Server(NetworkConfig.WRITE_BUFFER_SIZE, NetworkConfig.OBJECT_BUFFER_SIZE);
    
    public static void load() {
        try {
            NetworkObjectRegistry.load(SERVER.getKryo());
            SERVER.bind(PhotonEngine.network_Tcp, PhotonEngine.network_Udp);
            new Thread(SERVER, "Server Network Connection").start();
            SERVER.addListener(new MessageListenerServer());
        } catch (Exception e) {
        	ConsoleManager.create("Can't start Network Server: \n - " + e).withType(EnumLogType.NETWORK).displayOnDiscord().end();
        	e.printStackTrace();
        }
    }
    
    private static void closingServices() {
        ConsoleManager.create("Network shut down successfully!").withType(EnumLogType.NETWORK).displayOnDiscord().end();
    	if(SERVER !=null) SERVER.close();
    }
    
    public static void restart() {
    	closingServices();
    	ApplicationUtils.restart(NetworkLinkManager.class, TIME_BEFORE_STOP, new String[] { PhotonEngine.network_Ip });
    }
    
    public static void shutdown() {
		closingServices();
		ApplicationUtils.exitProperly(TIME_BEFORE_STOP);
	}
}
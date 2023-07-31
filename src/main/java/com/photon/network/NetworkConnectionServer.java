package com.photon.network;

import com.esotericsoftware.kryonet.Server;
import com.photon.PhotonEngine;
import com.photon.network.NetworkDirectories.NetworkConfig;
import com.photon.network.listeners.MessageListenerServer;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.os.ApplicationUtils;

public class NetworkConnectionServer
{
    public static Server server = new Server(NetworkConfig.writeBufferSize, NetworkConfig.objectBufferSize);
    public static long TIME_BEFORE_STOP = 2500L;
    
    public static void load() {
        try {
            NetworkObjectRegistry.load(server.getKryo());
            server.bind(PhotonEngine.network_Tcp, PhotonEngine.network_Udp);
            server.start();
            server.addListener(new MessageListenerServer());
        } catch (Exception e) {
        	ConsoleManager.print(EnumLogType.NETWORK, true, "Can't start Network Server: \n - " + e);
        	e.printStackTrace();
        }
    }
    
    private static void closingServices() {
    	ConsoleManager.print(EnumLogType.NETWORK, true, "Network shut down successfully!");
    	if(server !=null) server.close();
    }
    
    public static void restart() {
    	closingServices();
    	ApplicationUtils.restart(NetworkConnectionServer.class, TIME_BEFORE_STOP, new String[] { PhotonEngine.network_Ip });
    }
    
    public static void shutdown() {
		closingServices();
		ApplicationUtils.exitProperly(TIME_BEFORE_STOP);
	}
}

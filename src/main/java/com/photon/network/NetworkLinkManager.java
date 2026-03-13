package com.photon.network;

import com.esotericsoftware.kryonet.Server;
import com.photon.PhotonEngine;
import com.photon.network.NetworkDirectories.NetworkConfig;
import com.photon.network.listeners.MessageListenerServer;
import com.photon.util.NetworkOnly;
import com.photon.util.PhotonLogTypes;
import com.photon.util.os.ApplicationUtils;

import niwer.lumen.Console;

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
        	Console.log("Can't start Network Server: \n - " + e).type(PhotonLogTypes.NETWORK).sendToProcessor().container(PhotonEngine.LOGGER).send();
        	e.printStackTrace();
        }
    }
    
    private static void closingServices() {
        Console.log("Network shut down successfully!").type(PhotonLogTypes.NETWORK).sendToProcessor().container(PhotonEngine.LOGGER).send();
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
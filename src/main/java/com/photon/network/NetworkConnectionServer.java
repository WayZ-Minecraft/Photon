package com.photon.network;

import java.util.Timer;
import java.util.TimerTask;

import com.esotericsoftware.kryonet.Server;
import com.photon.PhotonEngine;
import com.photon.discord.DiscordEngine;
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
            new Thread(server).start();
            server.addListener(new MessageListenerServer());
            DiscordEngine.showNetworkPanel();
        } catch (Exception e) {
        	ConsoleManager.create("Can't start Network Server: \n - " + e).withType(EnumLogType.NETWORK).displayOnDiscord().end();
        	e.printStackTrace();
        }
    }
    
    private static void closingServices() {
        ConsoleManager.create("Network shut down successfully!").withType(EnumLogType.NETWORK).displayOnDiscord().end();
    	if(server !=null) server.close();
    	if(DiscordEngine.jda !=null) {
    		new Timer().schedule(new TimerTask() {
    			public void run() { DiscordEngine.jda.shutdownNow(); }
    		}, TIME_BEFORE_STOP);
    	}
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

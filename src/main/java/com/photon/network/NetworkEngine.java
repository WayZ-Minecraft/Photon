package com.photon.network;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.discord.BotEngine;
import com.photon.discord.OLDDiscordEngine;
import com.photon.informations.PhotonInfosManager;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class NetworkEngine {

	public static void main(final String[] args) {
		try {			
			PhotonEngine.setIP(PhotonInfosManager.getCurrentIP());

			/* Check ip */
			ConsoleManager.print(EnumLogType.NETWORK, "Starting Network Server on \"" + PhotonEngine.network_Ip + "\"!");
			if(!PhotonEngine.network_Ip.isEmpty() && !PhotonEngine.network_Ip.equalsIgnoreCase(PhotonEngine.network_Ip_Local) && !PhotonInfosManager.isIPEquals(PhotonEngine.network_Ip)) {
				ConsoleManager.print(EnumLogType.NETWORK, "Ip doesn't match. Closing Network!");
				System.exit(0);
				return;
			}

			/* Load features */
			NetworkDirectories.load();
			BotEngine.load();
			NetworkConnectionServer.load();

			/* Register logs file */
			ConsoleManager.registerFileHandler(new File(NetworkDirectories.logsDirectory, "network.log"));
		} catch(Exception e) { e.printStackTrace(); }
    }
	
	public static Connection getPlayerConnection(String uuid) { return (uuid == null || uuid.isEmpty()) ? null : PhotonEngine.networkConnectionsList.get(uuid); }
	
	public static String getPlayerUUID(Connection connection) {
        for(Entry<String, Connection> entry : PhotonEngine.networkConnectionsList.entrySet()) {
        	if(entry.getValue().equals(connection)) return entry.getKey();
        }
        return "";
    }
	
	public static List<Connection> getConnectedConnection() {
    	List<Connection> list = new ArrayList<>();
        for (Entry<String, Connection> entry : PhotonEngine.networkConnectionsList.entrySet()) {
        	final Connection conn = entry.getValue();
            if(conn != null && conn.isConnected()) list.add(conn);
        }
        return list;
    }
}

package com.photon.network;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.discord.BotEngine;
import com.photon.informations.PhotonInfosManager;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class NetworkEngine {

	public static void main(final String[] args) {
		try {
            /* Register logs file */
			ConsoleManager.registerFileHandler(new File(NetworkDirectories.logsDirectory, "network.log"));
            
            /* Connecting */
			PhotonEngine.setIP(PhotonInfosManager.getCurrentIP());
            
			/* Check ip */
            ConsoleManager.create("Starting Network Server on \"" + PhotonEngine.network_Ip + "\"!").withType(EnumLogType.NETWORK).end();
			if(!PhotonEngine.network_Ip.isEmpty() && !PhotonEngine.network_Ip.equalsIgnoreCase(PhotonEngine.network_Ip_Local) && !PhotonInfosManager.isIPEquals(PhotonEngine.network_Ip)) {
                ConsoleManager.create("Ip doesn't match. Closing Network!").withType(EnumLogType.NETWORK).error().end();
				System.exit(0);
				return;
			}

			/* Load features */
			NetworkDirectories.load();
			BotEngine.load(Arrays.asList(args).contains("--restart") ? "--restart" : null);
			NetworkConnectionServer.load();
		} catch(Exception e) { e.printStackTrace(); }
    }
	
    /**
     * Get the connection of a player
     * @param uuid : The uuid of the player
     * @return The connection of the player
     * @see Connection
     * @author Niwer
     */
	public static Connection getPlayerConnection(String uuid) { return (uuid == null || uuid.isEmpty()) ? null : PhotonEngine.networkConnectionsList.get(uuid); }
	
    /**
     * Get the uuid of a player from his connection
     * @param uuid : The uuid of the player
     * @return The connection of the player
     * @see Connection
     * @author Niwer
     */
	public static String getPlayerUUID(Connection connection) {
        for(Entry<String, Connection> entry : PhotonEngine.networkConnectionsList.entrySet()) {
        	if(entry.getValue().equals(connection)) return entry.getKey();
        }
        return "";
    }
	
    /**
     * Get the list of all connected connections
     * @return The list of all connected connections
     * @see Connection
     * @author Niwer
     */
	public static List<Connection> getConnectedConnection() {
    	List<Connection> list = new ArrayList<>();
        for (Entry<String, Connection> entry : PhotonEngine.networkConnectionsList.entrySet()) {
        	final Connection conn = entry.getValue();
            if(conn != null && conn.isConnected()) list.add(conn);
        }
        return list;
    }
}

package com.photon.network;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.discord.BotEngine;
import com.photon.network.objects.ObjectNews;
import com.photon.network.objects.ObjectServer;
import com.photon.network.sql.SqlInteract;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.NetworkOnly;

@NetworkOnly
public class NetworkEngine {

    /* Network saves */
    public static final List<ObjectNews> SAVED_NEWS_LIST = new ArrayList<>();
    public static final List<ObjectServer> SAVED_SERVER_LIST = new ArrayList<>();
    public static final Map<String, Connection> CONNECTED_CLIENTS_LIST = new HashMap<>();

	public static void main(final String[] args) {
		try {
            /* Load features */
			NetworkDirectories.load();
			
			/* Debug: Check what was loaded from config */
			String token = NetworkDirectories.getConfig().discordBotToken;
			if (token != null && !token.isEmpty()) {
				// Mask token for security (show only first 10 chars)
				String maskedToken = token.length() > 10 ? token.substring(0, 10) + "..." : token;
				ConsoleManager.create("Discord token loaded: " + maskedToken).withType(EnumLogType.NETWORK).end();
			} else {
				ConsoleManager.create("Discord token is null or empty").withType(EnumLogType.NETWORK).end();
			}

            /* Connect to SQL database */
            SqlInteract.connect();
            
            /* Register logs file */
			ConsoleManager.registerFileHandler(new File(NetworkDirectories.logsDirectory, "network.log"), "network");
            
            /* Connecting */
			PhotonEngine.setIP(PhotonEngine.getCurrentIP());
            
			/* Check ip */
            ConsoleManager.create("Starting Network Server on \"" + PhotonEngine.network_Ip + "\"!").withType(EnumLogType.NETWORK).end();
			if(!PhotonEngine.network_Ip.isEmpty() && !PhotonEngine.network_Ip.equalsIgnoreCase(PhotonEngine.network_Ip_Local) && !PhotonEngine.isIPEquals(PhotonEngine.network_Ip)) {
                ConsoleManager.create("Ip doesn't match. Closing Network!").withType(EnumLogType.NETWORK).error().end();
				System.exit(0);
				return;
			}

            /* Starting the discord bot if token available */
			if(NetworkDirectories.getConfig().discordBotToken !=null && !NetworkDirectories.getConfig().discordBotToken.isEmpty()) {
                try {
                    if (Arrays.asList(args).contains("--restart")) {
                        BotEngine.load("--restart");
                    } else {
                        BotEngine.load();
                    }
                    ConsoleManager.create("Discord Bot started successfully").withType(EnumLogType.NETWORK).end();
                } catch (Exception e) {
                    ConsoleManager.create("Failed to start Discord Bot: " + e.getMessage()).withType(EnumLogType.NETWORK).error().end();
                    e.printStackTrace();
                }
            } else {
                ConsoleManager.create("Discord Bot token not configured, skipping bot startup").withType(EnumLogType.NETWORK).end();
            }

			NetworkLinkManager.load();
            ConsoleManager.create("Network Server is now running and waiting for connections...").withType(EnumLogType.NETWORK).end();
		} catch(Exception e) { e.printStackTrace(); }
    }
	
    /**
     * Get the connection of a player
     * @param uuid : The uuid of the player
     * @return The connection of the player
     * @see Connection
     * @author Niwer
     */
	public static Connection getPlayerConnection(String uuid) { return (uuid == null || uuid.isEmpty()) ? null : CONNECTED_CLIENTS_LIST.get(uuid); }
	
    /**
     * Get the uuid of a player from his connection
     * @param uuid : The uuid of the player
     * @return The connection of the player
     * @see Connection
     * @author Niwer
     */
	public static String getPlayerUUID(Connection connection) {
        for(Entry<String, Connection> entry : CONNECTED_CLIENTS_LIST.entrySet()) {
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
    	final List<Connection> list = new ArrayList<>();
        for (Entry<String, Connection> entry : CONNECTED_CLIENTS_LIST.entrySet()) {
        	final Connection conn = entry.getValue();
            if(conn != null && conn.isConnected()) list.add(conn);
        }
        return list;
    }
}
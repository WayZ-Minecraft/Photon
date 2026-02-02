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

	public static void main(final String[] args) { load(args); }
    
    /**
     * Load the Network Engine
     * This method exist because it allows to start the NetworkEngine from {@link com.photon.NetworkClientSideTester#main(String[] args)}
     * @param args The arguments passed to the main method
     * @author Niwer
     */
	public static void load(final String[] args) {
		try {
            /* Load features */
			NetworkDirectories.load();
            NetworkDirectories.loadLogoOnServer();

            /* Connect to SQL database */
            SqlInteract.connect();
            
            /* Register logs file */
			ConsoleManager.registerFileHandler(new File(NetworkDirectories.logsDirectory, "network.log"), "network");
            
            /* Connecting */
			PhotonEngine.setIP(PhotonEngine.getCurrentIP());
            
			/* Check ip */
            ConsoleManager.create("Starting Network Server on \"" + PhotonEngine.network_Ip + "\"!").withType(EnumLogType.NETWORK).end();
			if(!PhotonEngine.network_Ip.isEmpty() && !PhotonEngine.network_Ip.equalsIgnoreCase(PhotonEngine.LOCAL_IP) && !PhotonEngine.isIPEquals(PhotonEngine.network_Ip)) {
                ConsoleManager.create("Ip doesn't match. Closing Network!").withType(EnumLogType.NETWORK).error().end();
				System.exit(0);
				return;
			}

            /* Starting the discord bot if token available */
			if(NetworkDirectories.getConfig().discordBotToken !=null && !NetworkDirectories.getConfig().discordBotToken.isEmpty()) {
                try {
                    BotEngine.load(Arrays.asList(args).contains("--restart"));
                    ConsoleManager.create("Discord Bot started successfully").withType(EnumLogType.NETWORK).end();
                } catch (Exception e) {
                    ConsoleManager.create("Failed to start Discord Bot: " + e.getMessage()).withType(EnumLogType.NETWORK).error().end();
                    e.printStackTrace();
                }
            } else ConsoleManager.create("Discord Bot token not configured, skipping bot startup").withType(EnumLogType.NETWORK).end();

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
        return CONNECTED_CLIENTS_LIST.entrySet().stream().filter(entry -> entry.getValue().equals(connection)).findFirst().get().getKey();
    }
	
    /**
     * Get the list of all connected connections
     * @return The list of all connected connections
     * @see Connection
     * @author Niwer
     */
	public static List<Connection> getConnectedConnection() {
        return CONNECTED_CLIENTS_LIST.entrySet().stream().filter(entry -> entry.getValue() != null && entry.getValue().isConnected()).map(Entry::getValue).toList();
    }
}
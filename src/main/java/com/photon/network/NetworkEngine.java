package com.photon.network;

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
import com.photon.network.sql.SQLInteraction;
import com.photon.util.NetworkOnly;
import com.photon.util.PhotonLogTypes;
import com.photon.web.WebServerEngine;

import niwer.lumen.Console;
import niwer.lumen.LumenEngine;
import niwer.lumen.container.ConsoleFileManager;

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
            /* Register logger */
            LumenEngine.removeDefaultHandlers(); // Ensure Lumen is loaded properly
            LumenEngine.disablePrintingFromDefaultContainer(); // TODO : This ensure that we're not using the default container.
            ConsoleFileManager.registerFileFor(NetworkDirectories.LOGS_DIR, PhotonEngine.LOGGER, "network");

            /* Load features */
			NetworkDirectories.load();
            NetworkDirectories.loadLogoOnServer();

            /* Connect to SQL database */
            SQLInteraction.connect();
            
            /* Connecting */
			PhotonEngine.setIP(PhotonEngine.getCurrentIP());
            
			/* Check ip */
            Console.log("Starting Network Server on \"" + PhotonEngine.network_Ip + "\"!").type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
			if(!PhotonEngine.network_Ip.isEmpty() && !PhotonEngine.network_Ip.equalsIgnoreCase(PhotonEngine.LOCAL_IP) && !PhotonEngine.isIPEquals(PhotonEngine.network_Ip)) {
                Console.log("Ip doesn't match. Closing Network!").type(PhotonLogTypes.NETWORK).error().container(PhotonEngine.LOGGER).send();
				System.exit(0);
				return;
			}

            /* Starting the discord bot if token available */
			if(NetworkDirectories.getConfig().discord_bot_token !=null && !NetworkDirectories.getConfig().discord_bot_token.isEmpty()) {
                try {
                    BotEngine.load(Arrays.asList(args).contains("--restart"));
                    Console.log("Discord Bot started successfully").type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
                } catch (Exception e) {
                    Console.log("Failed to start Discord Bot: " + e.getMessage()).type(PhotonLogTypes.NETWORK).error().container(PhotonEngine.LOGGER).send();
                    e.printStackTrace();
                }
            } else Console.log("Discord Bot token not configured, skipping bot startup").type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();

            /* Starts the web API and Server */
            WebServerEngine.load();

            /* Start Krynet itself */
			NetworkLinkManager.load();
            Console.log("Network Server is now running and waiting for connections...").type(PhotonLogTypes.NETWORK).sendToProcessor().container(PhotonEngine.LOGGER).send();
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
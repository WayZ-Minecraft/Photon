package com.photon.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.discord.BotEngine;
import com.photon.informations.PhotonInfosManager;
import com.photon.informations.PhotonUpdaterManager;
import com.photon.informations.PhotonUpdaterManager.UpdateFileType;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.os.ApplicationUtils;

public class NetworkEngine {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @SuppressWarnings("resource")
	public static void main(final String[] args) {
		try {
            /* Load features */
			NetworkDirectories.load();

            /* Register logs file */
			ConsoleManager.registerFileHandler(new File(NetworkDirectories.logsDirectory, "network.log"));

            /* Auto-update the network */
            if(PhotonUpdaterManager.hasUpdate(UpdateFileType.NETWORK, new File("network.jar"))) {
                ApplicationUtils.exitProperly();
                return;
            }
            
            /* Connecting */
			PhotonEngine.setIP(PhotonInfosManager.getCurrentIP());
            
			/* Check ip */
            ConsoleManager.create("Starting Network Server on \"" + PhotonEngine.network_Ip + "\"!").withType(EnumLogType.NETWORK).end();
			if(!PhotonEngine.network_Ip.isEmpty() && !PhotonEngine.network_Ip.equalsIgnoreCase(PhotonEngine.network_Ip_Local) && !PhotonInfosManager.isIPEquals(PhotonEngine.network_Ip)) {
                ConsoleManager.create("Ip doesn't match. Closing Network!").withType(EnumLogType.NETWORK).error().end();
				System.exit(0);
				return;
			}

            /* Satrting the discord bot if token avalible */
			if(NetworkDirectories.config.discordBotToken !=null && !NetworkDirectories.config.discordBotToken.isEmpty())
                BotEngine.load(Arrays.asList(args).contains("--restart") ? "--restart" : null);

			NetworkConnectionServer.load();

            /* Set-up request connection system */
            final ServerSocket serverSocket = new ServerSocket(49554);
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket));
            }
		} catch(Exception e) { e.printStackTrace(); }
        finally {
            threadPool.shutdown(); // Arrête proprement le thread pool lorsque le serveur est arrêté
        }
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

    /* Represent a client connection */
    static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) { this.clientSocket = socket; }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                String request = reader.readLine();
                if(request.equalsIgnoreCase("getInfos")) {
                    final String response = NetworkDirectories.config.webUrl+";"+NetworkDirectories.config.webPassword+";"+NetworkDirectories.config.webUser;
                    writer.println(response);
                }
                clientSocket.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}

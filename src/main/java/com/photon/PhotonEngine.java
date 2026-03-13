package com.photon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.photon.discord.BotEngine;
import com.photon.network.ClientLinkManager;
import com.photon.network.messages.requests.ClientRequestSendDiscordLogs;
import com.photon.util.PhotonLogTypes;

import niwer.lumen.Console;
import niwer.lumen.LumenEngine;
import niwer.lumen.container.Container;

public class PhotonEngine {
    
    /* Logger */
    public static final Container LOGGER = LumenEngine.registerContainer("PhotonEngine").addProcessor((data, time, formattedMessage) -> {
        if(BotEngine.isBotInitialized()) BotEngine.log(data);
        else {
            final ClientRequestSendDiscordLogs REQUEST = new ClientRequestSendDiscordLogs(data);
            ClientLinkManager.sendTCP(REQUEST);
        }
    });

    /* Default network values */
	public static final String LOCAL_IP = "localhost";
	public static String network_Ip = LOCAL_IP;
    public static int network_Tcp = 54556;
    public static int network_Udp = 54556;

    private static volatile String currentIP = null;

    /**
	 * Get the current IP of the user using the Amazon AWS service
	 * @return the current IP of the user
     * @author Niwer & noz43
	 */
    public static synchronized String getCurrentIP() {
        if (currentIP != null) return currentIP;
        
        try {
            final URL whatismyip = new URI("http://checkip.amazonaws.com").toURL();
            final BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            currentIP = in.readLine();
            in.close();
            return currentIP;
        } catch (IOException | URISyntaxException e) {}
        return "UNKNOWN";
    }
    
	/**
	 * Check if the current IP is equals to the given IP
	 * @param ip the IP to check
	 * @return true if the current IP is equals to the given IP
     * @author Niwer
	 */
    public static boolean isIPEquals(String ip) { return ip.equalsIgnoreCase(getCurrentIP()); }
    
	/**
	 * Check if the given IP is online (ping)
	 * @param op the IP to check
	 * @return true if the given IP is online
	 * @throws UnknownHostException : if the IP is not valid
	 * @throws IOException : if the IP is not reachable
     * @author Niwer
	 */
	public static boolean isOnline(String op) throws UnknownHostException, IOException { return InetAddress.getByName(op).isReachable(100); }

    /**
     * Get the current date in the official format
     * @param showTime if true, the time will be added to the date
     * @param date the date to format, if null, the current date will be used
     * @return The formatted current date as a String
     */
    public static String getDate(boolean showTime) { return getDate(showTime, new Date()); }
    
    /**
     * Get the date in the official format
     * @param showTime if true, the time will be added to the date
     * @param date the date to format, if null, the current date will be used
     * @return The formatted date as a String
     */
    public static String getDate(boolean showTime, Date date) {
        return new SimpleDateFormat("dd-mm-yyyy"+(showTime?"_HH-mm-ss":"")).format(date);
    }

    /**
     * This allow to connect to the network, if unable to connect to the server, it won't try connecting to a local server
     * @param ip the IP of the server (V.P.S)
     * @throws IOException if the connection failed on the local server too
     */
    public static void loadClient(String ip) throws IOException { loadClient(ip, false); }

    /**
     * This allow to connect to the network, if unable to connect to the server, it will try to connect to the local server (Development mode)
     * @param ip the IP of the server (V.P.S)
     * @param localHostFallback if true, the local server will be used if the connection failed
     * @throws IOException if the connection failed on the local server too
     */
    public static void loadClient(String ip, boolean localHostFallback) throws IOException {
        try {
            LumenEngine.removeDefaultHandlers(); // Ensure Lumen is loaded properly
    		PhotonEngine.setIP(ip);
    		ClientLinkManager.load();
    	} catch (IOException e) {
            Console.log("Unable to connect to "+ip+ (localHostFallback ? ". Fallback to localhost" : "")).type(PhotonLogTypes.NETWORK).error().container(PhotonEngine.LOGGER).send();
    		if(localHostFallback) {
                try {
                    PhotonEngine.setIP(LOCAL_IP);
                    ClientLinkManager.load();
                } catch(IOException ex) { throw ex; }
            }
    	}
    }
    
    /**
     * Set the IP of the network
     * @param ip the IP to set
     * @author Niwer
     */
    public static void setIP(String ip) { network_Ip = ip; }
    
    /**
     * Set the ports of the network
     * @param tcp The TCP protocol port
     * @param udp The UDP protocol port
     */
    public static void setPorts(int tcp, int udp) {
    	network_Tcp = tcp;
    	network_Udp = udp;
    }
}
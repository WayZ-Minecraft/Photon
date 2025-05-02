package com.photon;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.ObjectNews;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ObjectServer;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class PhotonEngine {
	
    /* API Version */
	public static final String VERSION = "1.0.0";

    /* Default network values */
	public static final String network_Ip_Local = "localhost";
	public static String network_Ip = network_Ip_Local;
    public static int network_Tcp = 54556;
    public static int network_Udp = 54556;
    
    /* Allow access to the player profile if requested before */
    public static final Object clientPlayerProfileWaiter = new Object();
    public static ObjectPlayerAccount clientPlayerProfile = new ObjectPlayerAccount();

    /* Allow access to the player account if requested before */
    public static final Object clientAccountResponseWaiter = new Object();
    public static ServerResponseValidAccount clientAccountResponse = new ServerResponseValidAccount();
    
    /* Allow access to the news list if requested before */
    public static final Object clientNewsListWaiter = new Object();
    public static ArrayList<ObjectNews> clientNewsList = new ArrayList<>();

    /* Allow access to the servers list if requested before */
    public static final Object clientServerListWaiter = new Object();
    public static ArrayList<ObjectServer> clientServerList = new ArrayList<>();
    
    /* Network saves */
    public static ArrayList<ObjectNews> networkNewsList = new ArrayList<>();
    public static ArrayList<ObjectServer> networkServerList = new ArrayList<>();
    public static HashMap<String, Connection> networkConnectionsList = new HashMap<>();

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
     * This allow to connect to the network, if unable to connect to the server, it will try to connect to the local server
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
    		PhotonEngine.setIP(ip);
    		NetworkConnectionClient.load();
    	} catch (IOException e) {
            ConsoleManager.create("Unable to connect to "+ip+ (localHostFallback ? ". Fallback to localhost" : "")).withType(EnumLogType.NETWORK).error().end();
    		if(localHostFallback) {
                try {
                    PhotonEngine.setIP(network_Ip_Local);
                    NetworkConnectionClient.load();
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

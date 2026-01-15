package com.photon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.photon.network.ClientLinkManager;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.ObjectNews;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ObjectServer;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class PhotonEngine {
	
    /* Default network values */
	public static final String LOCAL_IP = "localhost";
	public static String network_Ip = LOCAL_IP;
    public static int network_Tcp = 54556;
    public static int network_Udp = 54556;
    
    /* Allow access to the player profile if requested before */
    public static final Object clientPlayerProfileWaiter = new Object();
    public static ObjectPlayerAccount clientPlayerProfile = new ObjectPlayerAccount();

    /* Allow access to the player account if requested before */
    public static final Object clientAccountResponseWaiter = new Object();
    public static ServerResponseValidAccount clientAccountResponse;
    
    /* Allow access to the news list if requested before */
    public static final Object clientNewsListWaiter = new Object();
    public static ArrayList<ObjectNews> clientNewsList = new ArrayList<>();

    /* Allow access to the servers list if requested before */
    public static final Object clientServerListWaiter = new Object();
    public static ArrayList<ObjectServer> clientServerList = new ArrayList<>();
    
    public static final Object clientUpdateWaiter = new Object();
    public static byte[] updateData = new byte[0];
    public static String updateSha = "UNKNOWN";

    /**
	 * Get the current IP of the user using the Amazon AWS service
	 * @return the current IP of the user
     * @author Niwer
	 */
    public static String getCurrentIP() {    	
    	try {
    		final URL whatismyip = new URL("http://checkip.amazonaws.com");
    		final BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
    		String result = in.readLine();
    		in.close();
    		return result;
    	} catch (IOException e) {}
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
    		ClientLinkManager.load();
    	} catch (IOException e) {
            ConsoleManager.create("Unable to connect to "+ip+ (localHostFallback ? ". Fallback to localhost" : "")).withType(EnumLogType.NETWORK).error().end();
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
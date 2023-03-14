package com.photon;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.ObjectNews;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ObjectServer;
import com.photon.util.ConsoleManager;
import com.photon.util.os.OperatingSystem;

public class PhotonEngine {
	
	public static final String VERSION = "1.0.0";
	public static final String network_Ip_Local = "localhost";
	public static String network_Ip = network_Ip_Local;
    public static int network_Tcp = 54555;
    public static int network_Udp = 54555;
    
    public static ObjectPlayerAccount clientPlayerProfile;
    public static ServerResponseValidAccount clientAccountResponse;
    public static ArrayList<ObjectNews> clientNewsList = new ArrayList<>();
    public static ArrayList<ObjectServer> clientServerList = new ArrayList<>();
    
    public static ArrayList<ObjectNews> networkNewsList = new ArrayList<>();
    public static ArrayList<ObjectServer> networkServerList = new ArrayList<>();
    public static HashMap<String, Connection> networkConnectionsList = new HashMap<>();

    public static void loadClient(String ip) throws IOException {
    	try {
    		PhotonEngine.setIP(ip);
    		NetworkConnectionClient.load();
    	} catch (IOException e) {
    		try {
    			PhotonEngine.setIP(network_Ip_Local);
    			NetworkConnectionClient.load();
    		} catch(IOException ex) { throw ex; }
    	}
    }
    
    public static void setIP(String ip) { network_Ip = ip; }
    
    public static void setPorts(int tcp, int udp) {
    	network_Tcp = tcp;
    	network_Udp = udp;
    }
    
    public static void openURL(String url) {
    	try { openURL(new URI(url)); } catch (URISyntaxException e) { ConsoleManager.printError("Can't open URL : " + e); }
    }
    
    public static void openURL(URI url) {
    	try {
    		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) { Desktop.getDesktop().browse(url); }
            else if (OperatingSystem.getCurrentPlatform() == OperatingSystem.LINUX) { Runtime.getRuntime().exec(new String[] { "xdg-open", url.getPath() }); }
            else if (OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX) { Runtime.getRuntime().exec(new String[] { "open", url.getPath() }); }
            else { JOptionPane.showMessageDialog(null, "Unable to open browser, please visit the URL:\n" + url, "Unable to open browser", 0); }
        } catch (IOException | RuntimeException e) {}
    }
}

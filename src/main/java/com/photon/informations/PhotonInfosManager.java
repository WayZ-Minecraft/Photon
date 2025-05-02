package com.photon.informations;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.ProtectorManager;

public class PhotonInfosManager {

	public static boolean isUpdating;
	public static boolean updateFinished;
	public static double updatePercentage;
    public static double updateSize;
    public static double updateSizeDownloaded;
	
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
     * Get the game logo from the web
     * @return the game logo from the web in a BufferedImage
     * @see getGameLogoInputStream()
     * @author Niwer
     */
	public static BufferedImage getGameLogo() {
		try {
			final InputStream stream = getGameLogoInputStream();
			final BufferedImage img = ImageIO.read(stream);
			stream.close();
			return img;
		} catch (IOException e) { e.printStackTrace(); return null; }
	}
	
    /**
     * Get the game logo from the web
     * @return the game logo from the web in a InputStream
     * @see getGameLogo()
     * @author Niwer
     */
	public static InputStream getGameLogoInputStream() {
		try {
			final URLConnection connection = new URL(NetworkDirectories.getConfig().webUrl + "project-logo.png").openConnection();
			ProtectorManager.addProperties(connection);
			connection.connect();
			return connection.getInputStream();
		} catch (IOException e) {}
		return null;
	}
	
    /**
     * Get infos from the web
     * @return All infos from the web in a ObjectInfos
     * @author Niwer
     */
	public static ObjectInfos getInfos() {
        try {
        	final URLConnection connection = new URL(NetworkDirectories.getConfig().webUrl + "infos.json").openConnection();
        	ProtectorManager.addProperties(connection);
    		connection.connect();
			final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			final ObjectInfos object = new Gson().fromJson(in, ObjectInfos.class);
			in.close();
			return object;
        } catch (IOException e) {
			ConsoleManager.create("Error when loading info.json file. If your not connected to the network, then it's the reason why.").withType(EnumLogType.NETWORK).error().end();
		}
        return null;
    }
}

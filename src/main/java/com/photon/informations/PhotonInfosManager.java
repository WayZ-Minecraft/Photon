package com.photon.informations;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
	 */
    public static boolean isIPEquals(String ip) { return ip.equalsIgnoreCase(getCurrentIP()); }
    
	/**
	 * Check if the given IP is online (ping)
	 * @param op the IP to check
	 * @return true if the given IP is online
	 * @throws UnknownHostException : if the IP is not valid
	 * @throws IOException : if the IP is not reachable
	 */
	public static boolean isOnline(String op) throws UnknownHostException, IOException { return InetAddress.getByName(op).isReachable(100); }
	
	/**
	 * Update the launcher from the given dir
	 * @param dir the dir where the launcher file is located
	 * @param ending the extension of the launcher file (JAR, EXE, ...)
	 */
	public static void updateLauncherFromDir(File dir) {
		if(isUpdating) { ConsoleManager.create("Can't download two files at the same time").end(); return; }
		isUpdating = true;
		updateFinished = false;
        new Thread() {
        	@Override
            public void run() {
		        download(NetworkDirectories.config.webUrl+"services_updates/launcher-"+getLatestLauncherUpdate()+".jar", new File(dir, "/launcher.jar"));
		        isUpdating = false;
		        updateFinished = true;
        	}
        }.start();
	}
	
	/**
	 * Check if the launcher has an update
	 * @param actualVersion the actual version of the launcher
	 * @return true if the launcher has an update
	 */
	public static boolean hasLauncherUpdate(String actualVersion) { return actualVersion.hashCode() != getLatestLauncherUpdate().hashCode(); }
	
	/**
	 * Get the latest version of the launcher
	 * @return the latest version of the launcher
	 */
	public static String getLatestLauncherUpdate() { return getInfos() == null || getInfos().launcher_version == null ? "UNKNOWN" : getInfos().launcher_version; }
	
	/**
	 * Update the mod from the given dir
	 * @param fileName the name of the mod file
	 * @param dir the dir where the mod file is located
	 * @param currentVersion the current version of the mod
	 */
    public static void updateMod(String fileName, File dir, String currentVersion) {
    	if(isUpdating) { ConsoleManager.create("Can't download two files at the same time").end(); return; }
        isUpdating = true;
        updateFinished = false;
        new Thread() {
        	@Override
            public void run() {
		        final boolean success = download(NetworkDirectories.config.webUrl + "services_updates/" + fileName + "-" + getLatestModUpdate() + ".jar", new File(dir, fileName + "-" + getLatestModUpdate() + ".jar"));
		        final File oldFile = new File(dir, "/" + fileName + "-" + currentVersion + ".jar");
		        if(oldFile.exists() && success) oldFile.delete();
		        isUpdating = false;
		        updateFinished = true;
        	}
        }.start();
    }
    
	/**
	 * Check if the mod has an update
	 * @param actualVersion the actual version of the mod
	 * @return true if the mod has an update
	 */
	public static boolean hasModUpdate(String actualVersion) {
		final String lastestVersion = getLatestModUpdate();
		return actualVersion.hashCode() != lastestVersion.hashCode() && lastestVersion != "UNKNOWN";
	}
	
	/**
	 * Get the latest version of the mod
	 * @return the latest version of the mod
	 */
	public static String getLatestModUpdate() { return getInfos() == null || getInfos().mod_version == null ? "UNKNOWN" : getInfos().mod_version; }
	
	public static String getLatestModURL() { return NetworkDirectories.config.webUrl + "services_updates/" + getInfos().project_id + "-" + getLatestModUpdate() + ".jar"; }
	
	public static String getLatestModSHA1() {
		try {
			URL url = new URL(NetworkDirectories.config.webUrl+"services_updates/get_sha1.php");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			ProtectorManager.addProperties(conn);
			conn.setRequestMethod("GET");
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String sha1 = reader.readLine();
			reader.close();
			return sha1;
		} catch (IOException e) {}
		return "UNKNOWN";
	}

	/* return true if successfull */
    public static boolean download(final String remotePath, final File localPath) {
        BufferedInputStream in = null;
        FileOutputStream out = null;
        try {
            final URLConnection conn = new URL(remotePath).openConnection();
            ProtectorManager.addProperties(conn);
            final long size = conn.getContentLength();
            in = new BufferedInputStream(conn.getInputStream());
            out = new FileOutputStream(localPath);
            final byte[] data = new byte[1024];
            double sumCount = 0.0;
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
                sumCount += count;
                if (size > 0L) {
                	updatePercentage = sumCount / size * 100.0;
                    updateSize = (double)(size / 1024L / 1024L);
                    if (updateSizeDownloaded == (int)(sumCount / 1024.0 / 1024.0)) continue;
                    updateSizeDownloaded = (int)(sumCount / 1024.0 / 1024.0);
                }
            }
            return true;
        }
        catch (IOException e) {
			e.printStackTrace();
			isUpdating = false;
		}
        finally {
        	try {
        		if (in != null) in.close();
        		if (out != null) out.close();
        	} catch (IOException e3) { e3.printStackTrace(); }
        }
        return false;
    }
	
	public static BufferedImage getGameLogo() {
		try {
			final InputStream stream = getGameLogoInputStream();
			final BufferedImage img = ImageIO.read(stream);
			stream.close();
			return img;
		} catch (IOException e) { e.printStackTrace(); return null; }
	}
	
	public static InputStream getGameLogoInputStream() {
		try {
			final URLConnection connection = new URL(NetworkDirectories.config.webUrl + "project-logo.png").openConnection();
			ProtectorManager.addProperties(connection);
			connection.connect();
			return connection.getInputStream();
		} catch (IOException e) {}
		return null;
	}
	
	public static ObjectInfos getInfos() {
        try {
        	final URLConnection connection = new URL(NetworkDirectories.config.webUrl + "infos.json").openConnection();
        	ProtectorManager.addProperties(connection);
    		connection.connect();
			final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			final ObjectInfos object = new Gson().fromJson(in, ObjectInfos.class);
			in.close();
			return object;
        } catch (IOException e) { ConsoleManager.create(ConsoleManager.of(e)).withType(EnumLogType.LAUNCHER).error().end(); }
        return null;
    }
}

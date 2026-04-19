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
import java.util.Arrays;
import java.util.Date;

import com.photon.discord.BotEngine;
import com.photon.sql.AnticheatTable;
import com.photon.sql.CrashReportTable;
import com.photon.sql.DiscordLogTable;
import com.photon.sql.DiscordProfileTable;
import com.photon.sql.HWIDTable;
import com.photon.sql.LicenseTable;
import com.photon.sql.NewsTable;
import com.photon.sql.PlayerAccountTable;
import com.photon.sql.ServerTable;
import com.photon.util.PhotonLogTypes;
import com.photon.web.WebServerEngine;

import niwer.lumen.Console;
import niwer.lumen.LumenEngine;
import niwer.lumen.container.ConsoleFileManager;
import niwer.lumen.container.Container;
import niwer.queryon.DataBase;

public class PhotonEngine {
    
    private static volatile String currentIP = null;
    
    public static final DataBase DATA_BASE = new DataBase(Directories.DATA_BASE_FILE);
    public static final Container LOGGER = LumenEngine.registerContainer("PhotonEngine").addProcessor((data, time, formattedMessage) -> {
        if(BotEngine.isBotInitialized()) BotEngine.log(data); // Print the log to the discord channel if the bot is initialized
    });

    /**
	 * Get the current IP of the user using the Amazon AWS service
	 * @return the current IP of the user
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
     * 
	 * @param ip the IP to check
	 * @return true if the current IP is equals to the given IP
	 */
    public static boolean isIPEquals(String ip) { return ip.equalsIgnoreCase(getCurrentIP()); }
    
	/**
	 * Check if the given IP is online (ping)
     * 
	 * @param op the IP to check
	 * @return true if the given IP is online
	 * @throws UnknownHostException : if the IP is not valid
	 * @throws IOException : if the IP is not reachable
	 */
	public static boolean isOnline(String op) throws UnknownHostException, IOException { return InetAddress.getByName(op).isReachable(100); }

    /**
     * Get the current date in the official format
     * 
     * @param showTime if true, the time will be added to the date
     * @param date the date to format, if null, the current date will be used
     * @return The formatted current date as a String
     */
    public static String getDate(boolean showTime) { return getDate(showTime, new Date()); }
    
    /**
     * Get the date in the official format
     * 
     * @param showTime if true, the time will be added to the date
     * @param date the date to format, if null, the current date will be used
     * @return The formatted date as a String
     */
    public static String getDate(boolean showTime, Date date) {
        return new SimpleDateFormat("dd-mm-yyyy"+(showTime?"_HH-mm-ss":"")).format(date);
    }

    public static void main(final String[] args) {
        /* Register logger */
        ConsoleFileManager.registerFileFor(Directories.LOGS_DIR, PhotonEngine.LOGGER, "network");

        /* Load features */
        Directories.load();

        /* Register tables to the Data Base */
        DATA_BASE
            .registerTable(NewsTable.class)

            /* Security */
            .registerTable(HWIDTable.class)
            .registerTable(LicenseTable.class)
            .registerTable(AnticheatTable.class)
            .registerTable(CrashReportTable.class)
            .registerTable(DiscordLogTable.class)

            /* User Accounts */
            .registerTable(PlayerAccountTable.class)
            .registerTable(DiscordProfileTable.class)
            .registerTable(ServerTable.class)
        ;
        
        /* Starting the discord bot if token available */
        if(Directories.getConfig().discord_bot_token !=null && !Directories.getConfig().discord_bot_token.isEmpty()) {
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
        Console.log("Network Server is now running and waiting for connections...").type(PhotonLogTypes.NETWORK).sendToProcessor().container(PhotonEngine.LOGGER).send();
    }
}
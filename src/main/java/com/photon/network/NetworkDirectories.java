package com.photon.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NetworkDirectories
{
	public static File baseDirectory = new File("./network/");
	public static File crashDirectory = new File(baseDirectory + "/crashs/");
	public static File anticheatDirectory = new File(baseDirectory + "/anticheat/");
	public static File profilesDirectory = new File(baseDirectory + "/profiles/");
	public static File newsDirectory = new File(baseDirectory + "/news/");
	public static File logsDirectory = new File(baseDirectory + "/logs/");
	public static File discordDirectory = new File(baseDirectory + "/discord/");

	public static NetworkConfig config = NetworkConfig.DEFAULT;
	public static File configFile = new File(baseDirectory + "/config.json");
	
	public static void load() {
		if (!baseDirectory.exists()) baseDirectory.mkdirs();
		if (!crashDirectory.exists()) crashDirectory.mkdirs();
		if (!anticheatDirectory.exists()) anticheatDirectory.mkdirs();
		if (!profilesDirectory.exists()) profilesDirectory.mkdirs();
		if (!newsDirectory.exists()) newsDirectory.mkdirs();
		if (!logsDirectory.exists()) logsDirectory.mkdirs();
		if (!discordDirectory.exists()) discordDirectory.mkdirs();
		
		try {
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			if (!configFile.exists()) {
				configFile.createNewFile();
				final FileWriter writer = new FileWriter(configFile);
				writer.write(gson.toJson(new NetworkConfig()));
				writer.close();
			}
			final BufferedReader reader = new BufferedReader(new FileReader(configFile));
			config = gson.fromJson(reader, NetworkConfig.class);
			reader.close();
		} catch (IOException e) {}
	}
	
	public static class NetworkConfig {
		protected static NetworkConfig DEFAULT = new NetworkConfig();

		public boolean isEmpty() { return this.equals(DEFAULT); }

		public static int writeBufferSize = 10 * 1024 * 1024;
		public static int objectBufferSize = 10 * 1024 * 1024;
		
		public String webUrl = "";
		public String webUser = "";
		public String webPassword = "";
		
		public String discordBotToken = "";
		public String discordBotChannelID = "";
		public String discordBotChannelID_LOG = "";
		public String channelID_LOG = "";
		public String channelID_GENERAL = "";
	}
}
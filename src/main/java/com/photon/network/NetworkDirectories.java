package com.photon.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.photon.informations.PhotonUpdaterManager.UpdateChannel;
import com.photon.informations.PhotonUpdaterManager.UpdateFileType;

public class NetworkDirectories
{
	public static File baseDirectory = new File("./network/");
	public static File crashDirectory = new File(baseDirectory + "/crashs/");
	public static File anticheatDirectory = new File(baseDirectory + "/anticheat/");
	public static File profilesDirectory = new File(baseDirectory + "/profiles/");
	public static File newsDirectory = new File(baseDirectory + "/news/");
	public static File logsDirectory = new File(baseDirectory + "/logs/");
	public static File discordDirectory = new File(baseDirectory + "/discord/");
	public static File sqlDirectory = new File(baseDirectory + "/sql/");

	public static final Object configWaiter = new Object();
	public static NetworkConfig config = NetworkConfig.DEFAULT;
	public static File configFile = new File(baseDirectory + "/config.json");
	
	public static NetworkConfig getConfig() {
		return config == null ? NetworkConfig.DEFAULT : config;
	}

    /**
     * Load all directories and files
     * @author Niwer & noz43
     */
	public static void load() {
		if (!baseDirectory.exists()) baseDirectory.mkdirs();
		if (!crashDirectory.exists()) crashDirectory.mkdirs();
		if (!anticheatDirectory.exists()) anticheatDirectory.mkdirs();
		if (!profilesDirectory.exists()) profilesDirectory.mkdirs();
		if (!newsDirectory.exists()) newsDirectory.mkdirs();
		if (!logsDirectory.exists()) logsDirectory.mkdirs();
		if (!discordDirectory.exists()) discordDirectory.mkdirs();
		if (!sqlDirectory.exists()) sqlDirectory.mkdirs();
		
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

	public static String getPath(UpdateFileType type, UpdateChannel channel) {
		return NetworkDirectories.getConfig().filePaths.get(type).get(channel);
	}

	/**
	 * Save all directories and files
	 * @note He save the config file
	 * @see {@link #configFile}
	 * @see {@link #config}
	 */
	public static void save() {
		try {
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			final FileWriter writer = new FileWriter(configFile);
			writer.write(gson.toJson(config));
			writer.close();
		} catch (IOException e) {}
	}
	
	public static class NetworkConfig {
		protected static NetworkConfig DEFAULT = new NetworkConfig();

		public boolean isEmpty() { return this.equals(DEFAULT); }

		public static int writeBufferSize = 10 * 1024 * 1024;
		public static int objectBufferSize = 10 * 1024 * 1024;

		public Map<UpdateFileType, Map<UpdateChannel, String>> filePaths = Map.of(
			UpdateFileType.MOD, Map.of(
				UpdateChannel.STABLE, baseDirectory.getPath()+"/services_update/mod.jar",
				UpdateChannel.DEV, baseDirectory.getPath()+"/services_update/mod-dev.jar",
				UpdateChannel.TEST, baseDirectory.getPath()+"/services_update/mod-test.jar"
			),
			UpdateFileType.API, Map.of(
				UpdateChannel.STABLE, baseDirectory.getPath()+"/services_update/api.jar",
				UpdateChannel.DEV, baseDirectory.getPath()+"/services_update/api-dev.jar",
				UpdateChannel.TEST, baseDirectory.getPath()+"/services_update/api-test.jar"
			),
			UpdateFileType.NETWORK, Map.of(
				UpdateChannel.STABLE, baseDirectory.getPath()+"/services_update/network.jar",
				UpdateChannel.DEV, baseDirectory.getPath()+"/services_update/network-dev.jar",
				UpdateChannel.TEST, baseDirectory.getPath()+"/services_update/network-test.jar"
			),
			UpdateFileType.LAUNCHER, Map.of(
				UpdateChannel.STABLE, baseDirectory.getPath()+"/services_update/launcher.jar",
				UpdateChannel.DEV, baseDirectory.getPath()+"/services_update/launcher-dev.jar",
				UpdateChannel.TEST, baseDirectory.getPath()+"/services_update/launcher-test.jar"
			)
		);

		public String webUrl = "";
		
		@SerializedName("discord_bot_token")
		public String discordBotToken = "";

		public String channelsActiavtionKey = "FlKm1J2n3FgggE1*45$$f=1"; // Default activation key
		
		@SerializedName("api_version")
		public String api_version = "1.0.0";
		@SerializedName("mod_version")
		public String mod_version = "1.0.0";
		@SerializedName("launcher_version")
		public String launcher_version = "1.0.0";

		@SerializedName("discord_bot_id")
		public String discord_bot_id = "";

		@SerializedName("project_name")
		public String project_name = "MyProject";
		@SerializedName("project_id")
		public String project_id = "myproject";

		@SerializedName("twitter_url")
		public String twitter_url = "https://twitter.com/";
		@SerializedName("twitch_url")
		public String twitch_url = "https://twitch.tv/";
		@SerializedName("youtube_url")
		public String youtube_url = "https://youtube.com/";
		@SerializedName("discord_url")
		public String discord_url = "https://discord.gg/";
		@SerializedName("website_url")
		public String website_url = "https://example.com";

		@SerializedName("bootstrap_width")
		public int bootstrap_width = 1280;
		@SerializedName("bootstrap_height")
		public int bootstrap_height = 720;
	}
}
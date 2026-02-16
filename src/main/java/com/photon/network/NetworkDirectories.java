package com.photon.network;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.os.FileLocation;
import com.photon.util.updater.UpdateChannel;
import com.photon.util.updater.UpdateFileType;

public class NetworkDirectories
{
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();	
	private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();	

	public static final File baseDirectory = new File("./network/");
	public static final File logsDirectory = new File(baseDirectory + "/logs/");
	public static final File logoFile = new File(baseDirectory + "/project_logo.png");

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
		if (!logsDirectory.exists()) logsDirectory.mkdirs();
		
		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
				final FileWriter writer = new FileWriter(configFile);
				writer.write(PRETTY_GSON.toJson(new NetworkConfig()));
				writer.close();
			}
			final BufferedReader reader = new BufferedReader(new FileReader(configFile));
			config = PRETTY_GSON.fromJson(reader, NetworkConfig.class);
			reader.close();
		} catch (IOException e) {}
	}

	public static String getPathForUpdateChannel(UpdateFileType type, UpdateChannel channel) { return NetworkDirectories.getConfig().filePaths.get(type).get(channel); }

	/**
	 * Save all directories and files
	 * @note He save the config file
	 * @see {@link #configFile}
	 * @see {@link #config}
	 */
	public static void save() {
		try {
			final FileWriter writer = new FileWriter(configFile);
			writer.write(PRETTY_GSON.toJson(config));
			writer.close();
		} catch (IOException e) {}
	}
	
	/**
	 * Load game logo from file and serialize it to byte array (SERVER SIDE)
	 * @author noz43
	 * @author Niwer (Added : Gathering default logo if not found)
	 */
	public static void loadLogoOnServer() {
		if (!logoFile.exists()) {
			ConsoleManager.create("Game logo not found at: " + logoFile.getPath() + ". We'll try to gather the default one").withType(EnumLogType.NETWORK).end();

			final InputStream STREAM = FileLocation.loadFile("photon_logo.png");
			if (STREAM != null) {
				try {
					ImageIO.write(ImageIO.read(STREAM), "png", logoFile);
				} catch (IOException e) {
					ConsoleManager.create("Failed to create logo file at: " + logoFile.getPath()).error().withType(EnumLogType.NETWORK).end();
					e.printStackTrace();
					return;
				}
			}
		}
		
		try {
			if (!logoFile.exists()) {
				ConsoleManager.create("Game logo still not found, aborting logo load").error().withType(EnumLogType.NETWORK).end();
				return;
			}

			final BufferedImage IMAGE = ImageIO.read(logoFile);
			if (IMAGE == null) {
				ConsoleManager.create("Failed to read game logo").error().withType(EnumLogType.NETWORK).end();
				return;
			}
			
			final ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
			ImageIO.write(IMAGE, "png", BAOS);
			config.gameLogo = BAOS.toByteArray();
			BAOS.close();
			
			ConsoleManager.create("Game logo loaded successfully (" + config.gameLogo.length + " bytes)").withType(EnumLogType.NETWORK).end();
		} catch (IOException e) {
			ConsoleManager.create("Error loading game logo: " + e.getMessage()).error().withType(EnumLogType.NETWORK).end();
			e.printStackTrace();
		}
	}

	public static class NetworkConfig {
		protected static final NetworkConfig DEFAULT = new NetworkConfig();

		public boolean isEmpty() { return this.equals(DEFAULT); }

		public static final int WRITE_BUFFER_SIZE = 10 * 1024 * 1024;
		public static final int OBJECT_BUFFER_SIZE = 10 * 1024 * 1024;

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

		/* Logo */
		@SerializedName("main_logo") public byte[] gameLogo = null;
		
		/* Bot infos */
		@SerializedName("bot_activity") public String bot_activity = "/";
		@SerializedName("discord_bot_token") public String discord_bot_token = "";
		@SerializedName("discord_bot_id") public String discord_bot_id = "";
		@SerializedName("official_discord_server_id") public String official_discord_server_id = "";
		@SerializedName("network_console_channel_id") public String network_console_channel_id = "";
		
		/* Web Server */
		@SerializedName("webserver_port") public int webserver_port = 7070;

		/* Versions infos */
		@SerializedName("api_version") public String api_version = "1.0.0";
		@SerializedName("mod_version") public String mod_version = "1.0.0";
		@SerializedName("launcher_version") public String launcher_version = "1.0.0";

		@SerializedName("twitter_url") public String twitter_url = "https://twitter.com/";
		@SerializedName("twitch_url") public String twitch_url = "https://twitch.tv/";
		@SerializedName("youtube_url") public String youtube_url = "https://youtube.com/";
		@SerializedName("discord_url") public String discord_url = "https://discord.gg/";
		@SerializedName("website_url") public String website_url = "https://example.com";

		/**
		 * Get the game logo from the web
		 * @return the game logo from the web in a BufferedImage
		 * @see getGameLogoInputStream()
		 * @author Niwer
		 */
		public static BufferedImage getGameLogo() {
			try {
				final byte[] DATA = getConfig().gameLogo;
				if (DATA == null || DATA.length == 0) return null;
				
				final ByteArrayInputStream STREAM = new ByteArrayInputStream(DATA);
				final BufferedImage IMG = ImageIO.read(STREAM);
				STREAM.close();
				return IMG;
			} catch (IOException e) { e.printStackTrace(); return null; }
		}
		
		/**
		 * Get the game logo from the web
		 * @return the game logo from the web in a InputStream
		 * @see getGameLogo()
		 * @author Niwer
		 */
		public static InputStream getGameLogoInputStream() {
			final byte[] DATA = getConfig().gameLogo;
			if (DATA == null || DATA.length == 0) return null;
			return new ByteArrayInputStream(DATA);
		}
	}
}
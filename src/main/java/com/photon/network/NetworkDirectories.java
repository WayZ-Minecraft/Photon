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
import com.photon.util.updater.UpdateChannel;
import com.photon.util.updater.UpdateFileType;

public class NetworkDirectories
{
	public static final File baseDirectory = new File("./network/");
	public static final File logsDirectory = new File(baseDirectory + "/logs/");
	@Deprecated public static final File discordDirectory = new File(baseDirectory + "/discord/");
	public static final File sqlDirectory = new File(baseDirectory + "/sql/");
	public static final File logoFile = new File(baseDirectory + "/project-logo.png");

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

	public static String getPathForUpdateChannel(UpdateFileType type, UpdateChannel channel) { return NetworkDirectories.getConfig().filePaths.get(type).get(channel); }

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
	
	/**
	 * Load game logo from file and serialize it to byte array (SERVER SIDE)
	 * @author noz43
	 */
	public static void loadGameLogo() {
		if (!logoFile.exists()) {
			ConsoleManager.create("Game logo not found at: " + logoFile.getPath()).withType(EnumLogType.NETWORK).end();
			return;
		}
		
		try {
			BufferedImage image = ImageIO.read(logoFile);
			if (image == null) {
				ConsoleManager.create("Failed to read game logo").error().withType(EnumLogType.NETWORK).end();
				return;
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			config.gameLogo = baos.toByteArray();
			baos.close();
			
			ConsoleManager.create("Game logo loaded successfully (" + config.gameLogo.length + " bytes)").withType(EnumLogType.NETWORK).end();
		} catch (IOException e) {
			ConsoleManager.create("Error loading game logo: " + e.getMessage()).error().withType(EnumLogType.NETWORK).end();
			e.printStackTrace();
		}
	}
	
	/**
     * Get the game logo from the web
     * @return the game logo from the web in a BufferedImage
     * @see getGameLogoInputStream()
     * @author Niwer
     */
	public static BufferedImage getGameLogo() {
		try {
			byte[] logoData = getConfig().gameLogo;
			if (logoData == null || logoData.length == 0) return null;
			
			ByteArrayInputStream stream = new ByteArrayInputStream(logoData);
			BufferedImage img = ImageIO.read(stream);
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
		byte[] logoData = getConfig().gameLogo;
		if (logoData == null || logoData.length == 0) return null;
		return new ByteArrayInputStream(logoData);
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

		public byte[] gameLogo = null;
		
		/* Bot infos */
		@SerializedName("discord_bot_token") public String discordBotToken = "";
		@SerializedName("discord_bot_id") public String discord_bot_id = "";
		
		/* Versions infos */
		@SerializedName("api_version") public String api_version = "1.0.0";
		@SerializedName("mod_version") public String mod_version = "1.0.0";
		@SerializedName("launcher_version") public String launcher_version = "1.0.0";

		@SerializedName("twitter_url") public String twitter_url = "https://twitter.com/";
		@SerializedName("twitch_url") public String twitch_url = "https://twitch.tv/";
		@SerializedName("youtube_url") public String youtube_url = "https://youtube.com/";
		@SerializedName("discord_url") public String discord_url = "https://discord.gg/";
		@SerializedName("website_url") public String website_url = "https://example.com";
	}
}
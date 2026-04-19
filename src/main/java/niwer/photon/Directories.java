package niwer.photon;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.util.updater.UpdateChannel;
import niwer.photon.util.updater.UpdateFileType;

import niwer.lumen.Console;

public class Directories
{
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();	
	private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();	

	public static final File BASE_DIR = new File("./network/");
	public static final File LOGS_DIR = new File(BASE_DIR + "/logs/");
	public static final File LOGO_FILE = new File(BASE_DIR + "/project_logo.png");
	public static final File DATA_BASE_FILE = new File(BASE_DIR, "network.db");

	public static NetworkConfig config = NetworkConfig.DEFAULT;
	public static File configFile = new File(BASE_DIR + "/config.json");
	
	public static NetworkConfig getConfig() { return config == null ? NetworkConfig.DEFAULT : config; }

    /**
     * Load all directories and files
     */
	public static void load() {
		if (!BASE_DIR.exists()) BASE_DIR.mkdirs();
		if (!LOGS_DIR.exists()) LOGS_DIR.mkdirs();
		
		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
				try (var WRITER = new FileWriter(configFile)) { WRITER.write(PRETTY_GSON.toJson(NetworkConfig.DEFAULT)); }
			}
			final BufferedReader reader = new BufferedReader(new FileReader(configFile));
			config = PRETTY_GSON.fromJson(reader, NetworkConfig.class);
			reader.close();
		} catch (IOException e) {}
	}

	public static String getPathForUpdateChannel(UpdateFileType type, UpdateChannel channel) { return Directories.getConfig().filePaths.get(type).get(channel); }

	/**
	 * Save all directories and files
	 * 
	 * @note He save the config file
	 * @see {@link #configFile}
	 * @see {@link #config}
	 */
	public static void save() {
		try (var WRITER = new FileWriter(configFile)) {
			WRITER.write(PRETTY_GSON.toJson(config));
		} catch (IOException e) {}
	}
	
	public static String getOfficialLogoBase64() {
		try {
			/* Try to read the official logo file */
			final BufferedImage IMAGE = ImageIO.read(LOGO_FILE);
			if (IMAGE == null) {
				Console.log("Failed to read game logo").error().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
				return null;
			}
			
			/* Convert the image to a byte array */
			final ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
			ImageIO.write(IMAGE, "png", BAOS);
			BAOS.close();
			
			return Base64.getEncoder().encodeToString(BAOS.toByteArray()); // Encode the byte array to a Base64 string
		} catch (IOException e) {
			Console.log("Error loading game logo: " + e.getMessage()).error().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
			e.printStackTrace();
			return null;
		}	
	}
	
	public static class NetworkConfig {
		private static final NetworkConfig DEFAULT = new NetworkConfig();
		private static final String SERVICES_UPDATE_DIR = BASE_DIR.getPath() + "/services_update/";

		private static Map<UpdateChannel, String> updatePaths(String fileName) {
			return Map.of(
				UpdateChannel.STABLE, SERVICES_UPDATE_DIR + fileName + ".jar",
				UpdateChannel.DEV, SERVICES_UPDATE_DIR + fileName + "-dev.jar",
				UpdateChannel.TEST, SERVICES_UPDATE_DIR + fileName + "-test.jar"
			);
		}

		@SerializedName("file_paths")
		public Map<UpdateFileType, Map<UpdateChannel, String>> filePaths = Map.of(
			UpdateFileType.MOD, updatePaths("mod"),
			UpdateFileType.API, updatePaths("api"),
			UpdateFileType.NETWORK, updatePaths("network"),
			UpdateFileType.LAUNCHER, updatePaths("launcher")
		);
		
		/* Bot infos */
		@SerializedName("bot_activity") public String bot_activity = "/";
		@SerializedName("discord_bot_token") public String discord_bot_token = "";
		@SerializedName("discord_bot_id") public String discord_bot_id = "";
		@SerializedName("official_discord_server_id") public String official_discord_server_id = "";
		@SerializedName("network_console_channel_id") public String network_console_channel_id = "";
		@SerializedName("server_creator_role_id") public String server_creator_role_id = ""; // Only working on the official guild.
		
		/* Web Server */
		@SerializedName("webserver_port") public int webserver_port = 7070;

		/* Licensing */
		@SerializedName("license_product_id") public String license_product_id = "niwer-engine";
		@SerializedName("tebex_webhook_secret") public String tebex_webhook_secret = "";
		@SerializedName("license_default_duration_days") public long license_default_duration_days = 30L; // 30 days (1 month) default duration for licenses issued without an explicit expiration date

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
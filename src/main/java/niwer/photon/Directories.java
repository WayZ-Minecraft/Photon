package niwer.photon;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.google.gson.annotations.SerializedName;

import niwer.lumen.Console;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.util.os.OperatingSystem;
import niwer.photon.util.updater.UpdateChannel;
import niwer.photon.util.updater.UpdateFileType;

public class Directories
{
	public static final File BASE_DIR = new File("./network/");
	public static final File LOGS_DIR = new File(BASE_DIR + "/logs/");
	public static final File BACKUPS_DIR = new File(BASE_DIR + "/backups/");
	public static final File LOGO_FILE = new File(BASE_DIR + "/project_logo.png");
	public static final File DATA_BASE_FILE = new File(BASE_DIR, "network.db");

	public static NetworkConfig config = NetworkConfig.DEFAULT;
	public static File configFile = new File(BASE_DIR + "/config.json");
	
	public static NetworkConfig getConfig() { return config == null ? NetworkConfig.DEFAULT : config; }

	private Directories() {}

    /**
     * Load all directories and files
     */
	public static void load() {
		if (!BASE_DIR.exists()) BASE_DIR.mkdirs();
		if (!LOGS_DIR.exists()) LOGS_DIR.mkdirs();
		
		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
				try (var WRITER = new FileWriter(configFile)) { WRITER.write(GsonUtils.PRETTY_GSON.toJson(NetworkConfig.DEFAULT)); }
			}
			final BufferedReader reader = new BufferedReader(new FileReader(configFile));
			config = GsonUtils.PRETTY_GSON.fromJson(reader, NetworkConfig.class);
			reader.close();
			if (config == null) config = new NetworkConfig();
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
			getConfig();
			WRITER.write(GsonUtils.PRETTY_GSON.toJson(config));
		} catch (IOException e) {}
	}
	
	public static String getOfficialLogoBase64() {
		try {
			if(!LOGO_FILE.exists()) { // If there is no logo, then try to "download" the official one from the jar
				try(final InputStream STREAM = OperatingSystem.loadFile("photon_logo.png"); final FileOutputStream FOS = new FileOutputStream(LOGO_FILE)) {
					final BufferedImage IMAGE = ImageIO.read(STREAM); // Read from stream
					ImageIO.write(IMAGE, "png", FOS); // Write to file
				}
			}

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
		
		private static Map<UpdateChannel, String> updatePaths(String fileName) {
			final String SERVICES_UPDATE_DIR = BASE_DIR.getPath() + "/services_update/";
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
		@SerializedName("webserver_host") public String webserver_host = null; // null = all interfaces,
		@SerializedName("webserver_port") public int webserver_port = 7070;
		@SerializedName("webserver_api_key") public String webserver_api_key = UUID.randomUUID().toString(); // We're generating a random signature on the first launch, and then saving it in the config file. This signature is used to secure the endpoints, and should be kept secret.

		/* Database backups */
		@SerializedName("database_backup_enabled") public boolean database_backup_enabled = Boolean.TRUE;
		@SerializedName("database_backup_on_startup") public boolean database_backup_on_startup = Boolean.TRUE;
		@SerializedName("database_backup_interval_minutes") public long database_backup_interval_minutes = 1440L; // 1440 minutes (24 hours) default interval between automatic database backups
		@SerializedName("database_backup_file_prefix") public String database_backup_file_prefix = "db_backup";
		@SerializedName("database_backup_retention_days") public long database_backup_retention_days = 15L; // Keep backups for 15 days by default

		/* Licensing */
		@SerializedName("license_product_id") public String license_product_id = "niwer-engine";
		@SerializedName("license_default_duration_days") public long license_default_duration_days = 30L; // 30 days (1 month) default duration for licenses issued without an explicit expiration date

		/* Github */
		@SerializedName("github_pat") public String github_pat = ""; // Personal Access Token for GitHub API authentication
		@SerializedName("github_template_repo") public String github_template_repo = ""; // The template repository to use when creating new repositories for users
		@SerializedName("github_template_owner") public String github_template_owner = ""; // The owner of the template repository to use when creating new repositories for users
		@SerializedName("github_new_repo_owner") public String github_new_repo_owner = ""; // The owner of the new repository to create
		@SerializedName("github_customer_team") public String github_customer_team = "customers"; // The team slug for the "customers" team in the GitHub organization

		/* Mail */
		@SerializedName("mail_sender_email") public String mail_sender_email = "sender@example.org";
		@SerializedName("mail_smtp_host") public String mail_smtp_host = "";
		@SerializedName("mail_smtp_port") public int mail_smtp_port = 587;
		@SerializedName("mail_username") public String mail_username = "";
		@SerializedName("mail_password") public String mail_password = "";

		/* Stripe */
		@SerializedName("stripe_api_key") public String stripe_api_key = "";
		@SerializedName("stripe_webhook_secret") public String stripe_webhook_signature = "";

		/* Versions infos */
		@SerializedName("api_version") public String api_version = "1.0.0";
		@SerializedName("mod_version") public String mod_version = "1.0.0";
		@SerializedName("launcher_version") public String launcher_version = "1.0.0";

		@SerializedName("twitter_url") public String twitter_url = "https://twitter.com/";
		@SerializedName("twitch_url") public String twitch_url = "https://twitch.tv/";
		@SerializedName("youtube_url") public String youtube_url = "https://youtube.com/";
		@SerializedName("discord_url") public String discord_url = "https://discord.gg/";
		@SerializedName("website_url") public String website_url = "https://google.com";

		@SerializedName("store_url") public String store_url = "";
		@SerializedName("terms_of_service_url") public String terms_of_service_url = "";
		@SerializedName("terms_of_sale_url") public String terms_of_sale_url = "";
		@SerializedName("privacy_policy_url") public String privacy_policy_url = "";

		public boolean isEmpty() {
			return this.equals(NetworkConfig.DEFAULT);
		}

		public String dbBackupFilePrefix() { return database_backup_file_prefix != null && !database_backup_file_prefix.isBlank() ? database_backup_file_prefix : "db_backup"; }

		public boolean hasEmailConfig() {
			return mail_sender_email != null && !mail_sender_email.isBlank() &&
				mail_smtp_host != null && !mail_smtp_host.isBlank() &&
				mail_username != null && !mail_username.isBlank() &&
				mail_password != null && !mail_password.isBlank();
		}

		public boolean hasBotToken() {
			return discord_bot_token != null && !discord_bot_token.isBlank();
		}
	}
}
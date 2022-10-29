package com.photon.discord.richpresence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.photon.util.os.Arch;
import com.photon.util.os.OperatingSystem;
import com.sun.jna.Library;
import com.sun.jna.Native;

@SuppressWarnings("unused")
public final class DiscordRPC {

	private static final String DLL_VERSION = "3.4.0";
	private static final String LIB_VERSION = "1.6.2";

	static {
		loadDLL();
	}

	public static void discordInitialize(String applicationId, DiscordEventHandlers handlers, boolean autoRegister) {
		DLL.INSTANCE.Discord_Initialize(applicationId, handlers, autoRegister ? 1 : 0, null);
	}

	public static void discordRegister(String applicationId, String command) {
		DLL.INSTANCE.Discord_Register(applicationId, command);
	}

	public static void discordInitialize(String applicationId, DiscordEventHandlers handlers, boolean autoRegister, String steamId) {
		DLL.INSTANCE.Discord_Initialize(applicationId, handlers, autoRegister ? 1 : 0, steamId);
	}

	public static void discordRegisterSteam(String applicationId, String steamId) {
		DLL.INSTANCE.Discord_RegisterSteamGame(applicationId, steamId);
	}

	public static void discordUpdateEventHandlers(DiscordEventHandlers handlers) {
		DLL.INSTANCE.Discord_UpdateHandlers(handlers);
	}

	public static void discordShutdown() {
		DLL.INSTANCE.Discord_Shutdown();
	}

	public static void discordRunCallbacks() {
		DLL.INSTANCE.Discord_RunCallbacks();
	}

	public static void discordUpdatePresence(DiscordRichPresence presence) {
		DLL.INSTANCE.Discord_UpdatePresence(presence);
	}

	public static void discordClearPresence() {
		DLL.INSTANCE.Discord_ClearPresence();
	}

	public static void discordRespond(String userId, DiscordReply reply) {
		DLL.INSTANCE.Discord_Respond(userId, reply.reply);
	}

	private static void loadDLL() {
		String name = System.mapLibraryName("discord-rpc");
		String finalPath;
		String dir;

		if (OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX) { dir = "darwin"; }
		else if (OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS) { dir = (Arch.CURRENT == Arch.x64 ? "win-x64" : "win-x86"); }
		else { dir = "linux"; }

		finalPath = "/" + dir + "/" + name;

		try {
			File f = File.createTempFile("drpc", name);

			try (InputStream in = DiscordRPC.class.getResourceAsStream(finalPath); OutputStream out = openOutputStream(f)) {
				copyFile(in, out);
				f.deleteOnExit();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.load(f.getAbsolutePath());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void copyFile(final InputStream input, final OutputStream output) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		int n;
		while (-1 != (n = input.read(buffer))) { output.write(buffer, 0, n); }
	}

	private static FileOutputStream openOutputStream(final File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) { throw new IOException("File '" + file + "' exists but is a directory"); }
			if (!file.canWrite()) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			final File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file);
	}

	public enum DiscordReply {
		NO(0),
		YES(1),
		IGNORE(2);

		public final int reply;

		DiscordReply(int reply) {
			this.reply = reply;
		}
	}

	private interface DLL extends Library {
		DLL INSTANCE = Native.loadLibrary("discord-rpc", DLL.class);

		void Discord_Initialize(String applicationId, DiscordEventHandlers handlers, int autoRegister, String optionalSteamId);
		void Discord_Register(String applicationId, String command);
		void Discord_RegisterSteamGame(String applicationId, String steamId);
		void Discord_UpdateHandlers(DiscordEventHandlers handlers);
		void Discord_Shutdown();
		void Discord_RunCallbacks();
		void Discord_UpdatePresence(DiscordRichPresence presence);
		void Discord_ClearPresence();
		void Discord_Respond(String userId, int reply);
	}
}

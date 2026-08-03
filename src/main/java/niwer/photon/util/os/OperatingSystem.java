package niwer.photon.util.os;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public enum OperatingSystem {

	LINUX("linux", "unix"),
	WINDOWS("win", "windows"), 
	OSX("mac"),
	SOLARIS("solaris", "sunos"),
	UNKNOWN("unknown");

	private static final String SEPARATOR = System.getProperty("file.separator");
	public static final String CURRENT_OS_NAME = System.getProperty("os.name");

	public final String NAME;
	public final String[] NAME_ALIASES;

	OperatingSystem(String... aliases) {
		this.NAME = toString().toLowerCase();
		this.NAME_ALIASES = aliases;
	}

	public boolean isSupported() { return this != OperatingSystem.UNKNOWN; }

	public static String javaPath() {
		if (System.getProperty("os.name").toLowerCase().contains("win")) return "\"" + System.getProperty("java.home") + "\\bin\\java" + "\"";
		return System.getProperty("java.home") + "/bin/java";
	}

	public String javaDir() {
		final String JAVA_HOME = System.getProperty("java.home") + SEPARATOR + "bin" + SEPARATOR;
		if (currentPlatform() == OperatingSystem.WINDOWS && new File(JAVA_HOME + "javaw.exe").isFile()) return JAVA_HOME + "javaw.exe";
		return JAVA_HOME + "java";
	}

	public static OperatingSystem currentPlatform() {
		final String OS_NAME = System.getProperty("os.name").toLowerCase();
		for (final OperatingSystem os : values()) {
			for (final String alias : os.NAME_ALIASES) {
				if (OS_NAME.contains(alias)) return os;
			}
		}
		return OperatingSystem.UNKNOWN;
	}

	/**
	 * Load a file from the resources folder
	 * @param file The path of the file
	 * @return InputStream of the file
	 */
	public static InputStream loadFile(String file) { return ClassLoader.getSystemClassLoader().getResourceAsStream(file); }	
	
	/**
	 * Get the working directory for the application based on the operating system.
	 * 
	 * @param workDir The name of the working directory (e.g "Config")
	 * @return The working directory as a File object
	 */
	public static File getWorkingDirectory(String workDir) {
		final String USER_HOME = System.getProperty("user.home", ".");
		final File WORKING_DIR = switch (OperatingSystem.currentPlatform()) {
			case LINUX -> new File(USER_HOME + "/." + workDir);
			case SOLARIS -> new File(USER_HOME + "/." + workDir);
			case WINDOWS -> new File(USER_HOME + "\\AppData\\Roaming\\." + workDir);
			case OSX -> new File(USER_HOME + "/Library/" + workDir);
			default -> new File(USER_HOME + "/." + workDir);
		};
		if (!WORKING_DIR.exists()) WORKING_DIR.mkdirs();
		return WORKING_DIR;
	}

	/**
	 * Gets the HWID of the computer
	 * @return HWID
	 */
	public static String getHWID() {
		final String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
		return hash(toEncrypt);
    }

	/**
	 * Hashes the file
	 * @param toHash File
	 * @param algorithm Algorithm
	 * @return Hash
	 */
	public static String hash(File toHash, String algorithm) {
		if (toHash == null) return "unknown";
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] buffer = new byte[65536];
			try (FileInputStream stream = new FileInputStream(toHash)) {
				int read;
				while ((read = stream.read(buffer)) != -1) md.update(buffer, 0, read);
			}
			return HexFormat.of().formatHex(md.digest());
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Hashes the string with SHA-1
	 * @param toHash String
	 * @return Hash
	 */
	public static String hash(String toHash) { return hash(toHash, "SHA-1"); }

	/**
	 * Hashes the string with the specified algorithm
	 * @param toHash String
	 * @param algorithm Algorithm
	 * @return Hash
	 */
	public static String hash(String toHash, String algorithm) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			return HexFormat.of().formatHex(md.digest(toHash.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) { return ""; }
	}
}
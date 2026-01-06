package com.photon.util.os;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public enum OperatingSystem {

	LINUX(new String[] { "linux", "unix" }),
	WINDOWS(new String[] { "win", "windows" }), 
	OSX(new String[] { "mac" }),
	SOLARIS(new String[] { "solaris", "sunos" }),
	UNKNOWN(new String[] { "unknown" });

	/**
	 * The OS Name in System Properties
	 */
	public static final String NAME = System.getProperty("os.name");
	/**
	 * The name
	 */
	private final String name;
	/**
	 * The Os Aliases
	 */
	private final String[] aliases;

	/**
	 * The Constructor
	 * @param aliases The os aliases
	 */
	private OperatingSystem(String... aliases) {
		if (aliases == null) {
			throw new NullPointerException();
		}
		this.name = toString().toLowerCase();
		this.aliases = aliases;
	}

	public String getName() { return this.name; }

	public String[] getAliases() { return this.aliases; }

	/**
	 * @return If the current OS is a supported OS
	 */
	public boolean isSupported() { return this != OperatingSystem.UNKNOWN; }

	/**
	 * @return If is the current OS is an unsupported OS
	 */
	public boolean isUnsupported() { return this == UNKNOWN; }

	/**
	 * @return The Java Path
	 */
	public static String getJavaPath() {
		if (System.getProperty("os.name").toLowerCase().contains("win")) return "\"" + System.getProperty("java.home") + "\\bin\\java" + "\"";
		return System.getProperty("java.home") + "/bin/java";
	}

	/**
	 * @return The Java directory
	 */
	public String getJavaDir() {
		final String separator = System.getProperty("file.separator");
		final String path = System.getProperty("java.home") + separator + "bin" + separator;
		if (getCurrentPlatform() == OperatingSystem.WINDOWS && new File(path + "javaw.exe").isFile()) return path + "javaw.exe";
		return path + "java";
	}

	/**
	 * @return The current Platform
	 */
	public static OperatingSystem getCurrentPlatform() {
		final String osName = System.getProperty("os.name").toLowerCase();
		for (final OperatingSystem os : values()) {
			for (final String alias : os.getAliases()) {
				if (osName.contains(alias)) return os;
			}
		}
		return OperatingSystem.UNKNOWN;
	}

	/**
	 * Is this OS match with the part 
	 * @param part The Part to match
	 * @return If it match
	 */
	public static boolean match(String part) {
		if (part.contains(getCurrentPlatform().getName())) return true;
		for (String alias : getCurrentPlatform().getAliases()) {
			if (part.contains(alias)) return true;
		}
		return false;
	}

	/**
	 * @return The current Platform
	 */
	public static OperatingSystem getCurrent() {
		final String osName = NAME.toLowerCase();
		for (OperatingSystem os : values()) {
			for (String alias : os.aliases) {
				if (osName.contains(alias)) return os;
			}
		}
		return UNKNOWN;
	}
	
	public static String getCurrentNativesForOs(String nativeName) {
		final String osName = System.getProperty("os.name").toLowerCase();
		if (nativeName.contains(osName)) {
			//TODO: Check arch too
		}
		return "natives-windows";
	}

	/**
	 * Open a link
	 * @param link The string Url to open
	 */
	public static void openLink(final String link) {
		try { openLink(new URI(link)); } catch (URISyntaxException e) {}
	}

	/**
	 * Open a link
	 * @param link The Url to open
	 */
	public static void openLink(final URI link) {
		try {
			final Class<?> desktopClass = Class.forName("java.awt.Desktop");
			final Object o = desktopClass.getMethod("getDesktop", (Class[]) new Class[0]).invoke(null, new Object[0]);
			desktopClass.getMethod("browse", URI.class).invoke(o, link);
		} catch (Throwable e2) {
			if (getCurrentPlatform() == OperatingSystem.OSX) {
				try {
					Runtime.getRuntime().exec(new String[] { "/usr/bin/open", link.toString() });
				} catch (IOException e1) { System.out.println("Failed to open link " + link.toString()); }
			} else System.out.println("Failed to open link " + link.toString());
		}
	}
	
	/**
	 * Get the Java Bit
	 */
	public static Arch getJavaBit() {
		String res = System.getProperty("sun.arch.data.model");
		if (res != null && res.equalsIgnoreCase("64")) return Arch.x64;
		return Arch.x86;
	}

	/**
	 * Open a folder
	 * @param path The Folder Path
	 */
	public static void openFolder(final File path) {
		final String absolutePath = path.getAbsolutePath();
		final OperatingSystem os = getCurrentPlatform();
		if (os == OperatingSystem.OSX) {
			try {
				Runtime.getRuntime().exec(new String[] { "/usr/bin/open", absolutePath });
				return;
			} catch (IOException e) {
				System.out.println("Couldn't open " + path + " through /usr/bin/open");
			}
		}
		if (os == OperatingSystem.WINDOWS) {
			final String cmd = String.format("cmd.exe /C start \"Open file\" \"%s\"", absolutePath);
			try {
				Runtime.getRuntime().exec(cmd);
				return;
			} catch (IOException e2) {
				System.out.println("Couldn't open " + path + " through cmd.exe");
			}
		}
		try {
			final Class<?> desktopClass = Class.forName("java.awt.Desktop");
			final Object desktop = desktopClass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
			desktopClass.getMethod("browse", URI.class).invoke(desktop, path.toURI());
		} catch (Throwable e3) {
			System.out.println("Couldn't open " + path + " through Desktop.browse()");
		}
	}
}
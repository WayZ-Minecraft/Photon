package com.photon.util.os;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

	public boolean isUnsupported() { return this == UNKNOWN; }

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
	 * Is this OS match with the part 
	 * @param osNameToTest The name to match
	 * @return If it match
	 */
	public static boolean match(String osNameToTest) {
		if (osNameToTest.contains(currentPlatform().NAME)) return true;
		for (String alias : currentPlatform().NAME_ALIASES) {
			if (osNameToTest.contains(alias)) return true;
		}
		return false;
	}
	
	/**
	 * Get the natives name for the current platform
	 * 
	 * @return The natives name for the current platform (e.g. "natives-windows-x64")
	 */
	public static String natives() { return nativesFor("natives"); }

	/**
	 * Get the natives name for the current platform
	 * 
	 * @param nativeName The base name of the natives (e.g. "natives")
	 * @return The natives name for the current platform (e.g. "natives-windows-x64")
	 */
	public static String nativesFor(String nativeName) {
		final OperatingSystem OS = currentPlatform();
		final Arch ARCH = javaBit();

		return switch(OS) {
			case WINDOWS -> ARCH == Arch.x64 ? nativeName + "-windows-x64" : nativeName + "-windows";
			case LINUX -> ARCH == Arch.x64 ? nativeName + "-linux-x64" : nativeName + "-linux";
			case OSX -> ARCH == Arch.x64 ? nativeName + "-osx-x64" : nativeName + "-osx";
			default -> nativeName + "-windows";
		};
	}

	/**
	 * Open a link
	 * 
	 * @param link The String URL to open
	 */
	public static void openLink(final String link) {
		try { openLink(new URI(link)); } catch (URISyntaxException e) {}
	}

	/**
	 * Open a link
	 * 
	 * @param link The URI to open
	 */
	public static void openLink(final URI link) {
		try {
			final Class<?> DESKTOP = Class.forName("java.awt.Desktop");
			final Object o = DESKTOP.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
			DESKTOP.getMethod("browse", URI.class).invoke(o, link);
		} catch (Throwable e2) {
			if (currentPlatform() == OperatingSystem.OSX) {
				try {
					Runtime.getRuntime().exec(new String[] { "/usr/bin/open", link.toString() });
				} catch (IOException e1) { System.out.println("Failed to open link " + link.toString()); }
			} else System.out.println("Failed to open link " + link.toString());
		}
	}
	
	/**
	 * Get the Java Bit
	 */
	public static Arch javaBit() {
		final String ARCH = System.getProperty("sun.arch.data.model");
		if (ARCH != null && ARCH.equalsIgnoreCase("64")) return Arch.x64;
		return Arch.x86;
	}

	/**
	 * Open a folder
	 * 
	 * @param path The Folder Path
	 * @throws UnsupportedOperationException If the current OS is not supported
	 */
	public static void openFolder(final File path) {
		final String PATH = path.getAbsolutePath();
		final OperatingSystem OS = currentPlatform();
		final ProcessBuilder PROCESS = switch (OS) {
			case WINDOWS -> new ProcessBuilder("explorer.exe", PATH);
			case LINUX -> new ProcessBuilder("xdg-open", PATH);
			case OSX -> new ProcessBuilder("open", PATH);
			default -> null;
		};

		if (PROCESS == null) throw new UnsupportedOperationException("Unsupported operating system: " + OS.NAME);
		try {
			PROCESS.start();
		} catch (IOException e) {
			System.out.println("Couldn't open " + path + " through " + PROCESS.command().get(0));
		}
	}
}
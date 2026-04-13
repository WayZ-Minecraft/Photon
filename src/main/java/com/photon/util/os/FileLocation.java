package com.photon.util.os;

import java.io.File;
import java.io.InputStream;

public class FileLocation {
	
	/**
	 * Load a file from the resources folder
	 * @param file The path of the file
	 * @return InputStream of the file
	 */
	public static InputStream loadFile(String file) { return ClassLoader.getSystemClassLoader().getResourceAsStream(file); }	
	
	/**
	 * Get the working directory for the application based on the operating system.
	 * 
	 * @param workDir The name of the working directory (e.g "Photon")
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
}

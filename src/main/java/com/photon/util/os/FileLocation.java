package com.photon.util.os;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class FileLocation {

	public static BufferedImage loadImage(String image) {
		try {
			final InputStream stream = loadFile(image);
			if(stream == null) return null;
			final BufferedImage img = ImageIO.read(stream);
			stream.close();
			return img;
		} catch (IOException e) { return null; }
	}

	public static Font loadFont(String path, String fontName, float size) {
		Font font = null;
		try {
			final InputStream stream = loadFile(path);
			font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.PLAIN, size);
			stream.close();
		} catch (Exception e) {}
		return font;
	}
	
	/**
	 * Load a file from the resources folder
	 * @param file The path of the file
	 * @return InputStream of the file
	 */
	public static InputStream loadFile(String file) { return ClassLoader.getSystemClassLoader().getResourceAsStream(file); }	
	
	public static File getWorkingDirectory(String workDir) {
		String userHome = System.getProperty("user.home", ".");
		File workingDirectory;
		switch (OperatingSystem.getCurrentPlatform()) {
			case LINUX:
				workingDirectory = new File(userHome + "/." + workDir);
			case SOLARIS:
				workingDirectory = new File(userHome + "/." + workDir);
				break;
			case WINDOWS:
				workingDirectory = new File(userHome + "\\AppData\\Roaming\\." + workDir);
				break;
			case OSX:
				workingDirectory = new File(userHome + "/Library/" + workDir);
				break;
			default:
				workingDirectory = new File(userHome + "/." + workDir);
		}
		if (!workingDirectory.exists()) workingDirectory.mkdirs();
		return workingDirectory;
	}
}

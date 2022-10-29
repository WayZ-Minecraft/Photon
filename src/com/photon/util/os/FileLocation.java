package com.photon.util.os;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.photon.ui.images.Scalr;
import com.photon.ui.images.Scalr.Method;

public class FileLocation {
	
	public static Clip loadSound(String file) {
		try {
			final AudioInputStream audioIn = AudioSystem.getAudioInputStream(ClassLoader.getSystemClassLoader().getResource("resources/" + file + (file.contains(".wav") ? "" : ".wav")));
			final Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			return clip;
		} catch (Exception e) {}
		return null;
	}
	
	public static BufferedImage loadSmoothedImage(String image) { return loadSmoothedImage(image, 50); }
	
	public static BufferedImage loadSmoothedImage(String image, int smoothLevel) { return Scalr.resize(loadImage(image), Method.ULTRA_QUALITY, smoothLevel, Scalr.OP_ANTIALIAS); }
	
	public static BufferedImage loadImage(String image) {
		try { return ImageIO.read(loadFile(image)); } catch (IOException e) { return null; }
	}

	public static Font loadFont(String path, String fontName, float size) {
		Font font = null;
		try { font = Font.createFont(Font.TRUETYPE_FONT, loadFile(path)).deriveFont(Font.PLAIN, 15f); } catch (Exception e) { e.printStackTrace(); }
		return font.deriveFont(size);
	}
	
	public static InputStream loadFile(String file) { return ClassLoader.getSystemClassLoader().getResourceAsStream("resources/" + file); }	
	
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
				workingDirectory = new File(userHome + "/Library/Application Support/" + workDir);
				break;
			default:
				workingDirectory = new File(userHome + "/." + workDir);
		}
		if (!workingDirectory.exists()) { workingDirectory.mkdirs(); }
		return workingDirectory;
	}
}

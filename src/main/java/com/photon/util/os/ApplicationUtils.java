package com.photon.util.os;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.ArrayUtils;

public class ApplicationUtils {

	public static void restart(Class<?> clazz, String[] commands) { restart(clazz, 1500L, commands); }
	
	public static void restart(Class<?> clazz, long time, String[] commands) {
		try {
			final File currentJar = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
			launch(currentJar, commands, true, time);
		} catch (URISyntaxException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
	}
	
	public static void launch(File f, String[] commands, boolean exit) { launch(f, commands, exit, 1500L); }
	
	public static void launch(File f, String[] commands, boolean exit, long time) {
		if(!f.getName().endsWith(".jar")) { return; }
		final ProcessBuilder pb = new ProcessBuilder(ArrayUtils.addAll(new String[] { "java", "-jar", f.getAbsolutePath() }, commands));
		try {
			pb.start();
			if(exit) { exitProperly(time); }
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			if(exit) { exitProperly(time); }
		}
	}
    
	public static void exitProperly() { exitProperly(1500L); }
	
    public static void exitProperly(long time) {
		new Timer().schedule(new TimerTask() {
			public void run() { System.exit(0); }
		}, time);
	}
}

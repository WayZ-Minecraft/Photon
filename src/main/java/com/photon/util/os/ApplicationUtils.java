package com.photon.util.os;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

public class ApplicationUtils {

	public static void restart(Class<?> clazz, String[] commands) { restart(clazz, 1500L, commands); }
	
	public static void restart(Class<?> clazz, long time, String[] commands) {
		try {
			final File currentJar = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
			launch(currentJar, commands, true, time);
		} catch (URISyntaxException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
	}
	
	/**
	 * Launch a jar
	 * @param f The file to launch
	 * @param commands Additionals startup commands
	 * @param exit Should current app terminate
     * @autor Created by Niwer
	 */
	public static void launch(File f, String[] commands, boolean exit) { launch(f, commands, exit, 1500L); }
	
	/**
	 * Launch a jar
	 * @param f The file to launch
	 * @param commands Additionals startup commands
	 * @param exit Should current app terminate
	 * @param time The time in ms before exiting the command line
     * @autor Created by Niwer
	 */
	public static void launch(File f, String[] commands, boolean exit, long time) {
		final ArrayList<String> list = new ArrayList<>();
		
		/* Adding command lines */
		if(f.getName().endsWith(".jar")) list.addAll(Arrays.asList("java", "-jar", f.getAbsolutePath()));
		else list.add(f.getPath());
		list.addAll(Arrays.asList(commands));
		
		/* Start app */
		try { new ProcessBuilder(list.toArray(new String[] {})).start(); }
		catch (Exception e) { e.printStackTrace(); }
		if(exit) exitProperly(time);
	}
    
	public static void exitProperly() { exitProperly(1500L); }
	
    public static void exitProperly(long time) {
		new Timer().schedule(new TimerTask() {
			public void run() { System.exit(0); }
		}, time);
	}
}

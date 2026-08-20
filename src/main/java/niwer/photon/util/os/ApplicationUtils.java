package niwer.photon.util.os;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import niwer.lumen.Console;
import niwer.lumen.types.DefaultLogTypes;
import niwer.photon.PhotonEngine;

/**
 * @author Niwer
 */
public class ApplicationUtils {

	private ApplicationUtils() {}

	public static void restart(Class<?> clazz, String... commands) { restart(clazz, 1500L, commands); }
	
	/**
	 * Restart the current app
	 * @param clazz The class of the app (ex: Network: {@link PhotonEngine#main() PhotonEngine})
	 * @param time The time in ms before exiting the command line
	 * @param commands Additionals startup commands
	 */
	public static void restart(Class<?> clazz, long time, String[] commands) {
		try {
			final File CURRENT_JAR = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
			launch(CURRENT_JAR, commands, true, time);
		} catch (URISyntaxException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
	}
	
	/**
	 * Launch a jar
	 * @param f The file to launch
	 * @param commands Additionals startup commands
	 * @param exit Should current app terminate
	 * @param time The time in ms before exiting the command line
     * @author Created by Niwer
	 */
	public static void launch(File f, String[] commands, boolean exit, long time) {
		final ArrayList<String> list = new ArrayList<>();
		
		/* Adding command lines */
		if(f.getName().endsWith(".jar")) list.addAll(Arrays.asList("java", "-jar", f.getAbsolutePath()));
		else list.add(f.getPath());
		list.addAll(Arrays.asList(commands));

		/* Start app */
		try {
            final Process PROCESS = new ProcessBuilder(list.toArray(new String[0])).start();
            final int EXIT_VALUE = PROCESS.waitFor();
            Console.log("Exit with " + EXIT_VALUE).type(DefaultLogTypes.INFO).container(PhotonEngine.LOGGER).send();
        } catch (Exception e) { e.printStackTrace(); }

		/* Exit properly */
		if(exit) exitProperly(time);
	}
    	
    public static void exitProperly(long time) {
		new Timer().schedule(new TimerTask() {
			public void run() { System.exit(0); }
		}, time);
	}
}

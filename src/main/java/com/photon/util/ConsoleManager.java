package com.photon.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.photon.PhotonEngine;
import com.photon.discord.BotEngine;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.messages.requests.ClientRequestSendDiscordLogs;

public class ConsoleManager
{  	
	private static final ConsoleHandler defaultHandler = new ConsoleHandler();
	private static final Logger defaultLogger = Logger.getLogger("default");
	private static final Map<String, ConsoleHandler> handlers = new HashMap<>();
	private static final Map<String, Logger> loggers = new HashMap<>();

	/* Colors */
	protected static final String ANSI_RESET = "\u001B[241m";
	protected static final String ANSI_BLACK = "\u001B[30m";
	protected static final String ANSI_RED = "\u001B[31m";
	protected static final String ANSI_GREEN = "\u001B[32m";
	protected static final String ANSI_YELLOW = "\u001B[33m";
	protected static final String ANSI_BLUE = "\u001B[34m";
	protected static final String ANSI_PURPLE = "\u001B[35m";
	protected static final String ANSI_CYAN = "\u001B[36m";
	protected static final String ANSI_WHITE = "\u001B[37m";
	
	static {
		/* Remove existing handlers */
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		for (Handler handler : handlers) rootLogger.removeHandler(handler);
		
		/* Adding console handler */
		defaultLogger.addHandler(defaultHandler);

		// System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
	}
	
	public static ConsoleContainer createManager(String id) {
		Logger logger = Logger.getLogger(id);
		ConsoleHandler handler = new ConsoleHandler();

		/* Encode to UTF-8 */
		try { handler.setEncoding("UTF-8"); }
		catch (IOException e) { e.printStackTrace(); }

		logger.addHandler(handler);

		loggers.put(id, logger);
		handlers.put(id, handler);

		return new ConsoleContainer(id, handler, logger);
	}

	public static String of(Throwable t) {
		try {
			final StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			String result = sw.toString();
			sw.close();
			return new String(result.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
		} catch (IOException e) { return null; }
	}
	
	public static String of(Exception e) { return of((Throwable)e); }

	/**
	 * Register a file handler to save logs (This will be the default handler)
	 * @param file The file to save logs
	 */
	public static void registerFileHandler(File file, String savedFileName) { registerFileHandler(file, null, savedFileName); }

	/**
	 * Register a file handler to save logs
	 * @param file The file to save logs
	 * @param handler The handler to use
	 * @param savedFileName The name of the file to save (The result will be  SAVED_FILE_NAME-DATE.log)
	 */
	public static void registerFileHandler(File file, ConsoleContainer container, String savedFileName) {
		/* Adding file handler */
		try {
    		final FileHandler fh = new FileHandler(file.getPath());
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                ConsoleManager.create("Uncaught exception in thread " + t.getName() +"\n"+ of(e)).error().end();
            });
			if(container != null) container.getLogger().addHandler(fh);
			else defaultLogger.addHandler(fh);
            fh.setFormatter(new ConsoleFormatter(null));
            
			/* Save file after closing launcher */
			Runtime.getRuntime().addShutdownHook(new Thread(() -> { savePreviousFile(file, savedFileName); }, "ConsoleManager-SavePreviousFile"));
        } catch (SecurityException | IOException e) {}
	}
	
	private static void savePreviousFile(File file, String savedFileName) {
		try {
			Files.copy(Path.of(file.getPath()), Path.of(new File(file.getParent(), savedFileName+"-"+PhotonEngine.getDate(true)+".log").getPath()));
		} catch (IOException e) {}
	}

	/**
	 * Create a log to be displayed in the console
	 * @param obj The object to display
	 * @return The log for personnalization
	 */
	public static Log create(Object obj) { return new Log().withObject(obj); }

	/**
	 * Display a message in the console
	 * @param o The message to display
	 */
	public static void debug(Object o) { System.out.println(o); }

	public static void debug(boolean newLineOnEachObject, Object... objects) {
		for(Object o : objects) System.out.print(o + (newLineOnEachObject ? "\n" : " "));
		System.out.println();
	}
	
	private static boolean elipsedTime = false;
	private static long startTime = 0;
	
	/**
	 * Start the time elapsed
	 * @see #endTime(String)
	 */
	public static void startTime() {
		elipsedTime = true;
		startTime = System.currentTimeMillis();
	}

	/**
	 * Display the time elapsed since the last startTime() call
	 * @param message The message to display
	 * @see #startTime()
	 */
	public static void endTime(String message) {
		if(elipsedTime) {
			elipsedTime = false;
			ConsoleManager.create(message+" ("+(System.currentTimeMillis()-startTime)+"ms)").end();
		}
	}

	public static class Log {
		private EnumLogType type = EnumLogType.INFO;
		private boolean isError = false;
		private boolean logOnDiscord = false;
		private Object object;
		private File file;
		private ConsoleContainer container;

		public void end() {
			final String subTypeName = (isError?" : "+ConsoleManager.ANSI_RED+"ERROR"+type.consoleColor:"");
			
			if(container !=null) {
				/* Changing log format */
				if(!(container.getHandler().getFormatter() instanceof ConsoleFormatter)) container.getHandler().setFormatter(new ConsoleFormatter(type));
				
				/* Log */
				container.getLogger().log(Level.OFF, "["+type+subTypeName+"] "+object);
			} else {
				/* Changing log format */
				if(!(defaultHandler.getFormatter() instanceof ConsoleFormatter)) defaultHandler.setFormatter(new ConsoleFormatter(type));
	
				/* Log */
				defaultLogger.log(Level.OFF, "["+type+subTypeName+"] "+object);
			}

			/* Display the error on discord if enabled */
			if(logOnDiscord) {
				/* If on network -> Don't send packets */
				if(BotEngine.botBuilder !=null) BotEngine.log(isError? Color.RED :type.color, type+subTypeName, object, file);
				else {
					final ClientRequestSendDiscordLogs request = new ClientRequestSendDiscordLogs();
					request.type = type;
					request.subType = subTypeName;
					request.content = object;
					NetworkConnectionClient.sendTCP(request);
				}
			}
		}

		public Log withContainer(ConsoleContainer container) {
			this.container = container;
			return this;
		}

		public Log displayOnDiscord() {
			this.logOnDiscord = true;
			return this;
		}

		public Log withType(EnumLogType type) {
			this.type = type;
			return this;
		}

		protected Log withObject(Object obj) {
			this.object = obj;
			return this;
		}

		public Log error() {
			this.isError = true;
			return this;
		}

		public Log withFile(File file) {
			this.file = file;
			return this;
		}
	}
	
	public static enum EnumLogType {
		INFO(new Color(52, 148, 196), ANSI_CYAN), 
		NETWORK(new Color(178, 63, 63), ANSI_GREEN),
		LAUNCHER(new Color(98, 164, 83), ANSI_BLACK),
		ANTICHEAT(new Color(196, 148, 52), ANSI_YELLOW),
		CLIENT(new Color(98, 164, 83), ANSI_BLUE), 
		SERVER(new Color(98, 164, 83), ANSI_PURPLE);
		
		public Color color;
		public String consoleColor;
		
		private EnumLogType(Color color, String consoleColor) {
			this.color = color;
			this.consoleColor = consoleColor;
		}
	}
	
	private static class ConsoleFormatter extends Formatter {
		private final EnumLogType type;

		private ConsoleFormatter(EnumLogType type) { this.type = type; }

        @Override
        public String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
            if(type !=null) {
				sb.append(type.consoleColor);
				sb.append(record.getMessage());
				sb.append("\n");
				sb.append(ANSI_WHITE);
			} else {
				sb.append(new String(record.getMessage().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
				sb.append("\n");
			}
            return sb.toString();
        }
    }

	public static class ConsoleContainer {
		private final String id;
		private final ConsoleHandler handler;
		private final Logger logger;

		public ConsoleContainer(String id, ConsoleHandler handler, Logger logger) {
			this.id = id;
			this.handler = handler;
			this.logger = logger;
		}

		public String getId() { return id; }

		public ConsoleHandler getHandler() { return handler; }

		public Logger getLogger() { return logger; }
	}
}
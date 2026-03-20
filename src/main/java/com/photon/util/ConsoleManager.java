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
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jetbrains.annotations.Nullable;

import com.photon.PhotonEngine;

import niwer.lumen.Console;

/**
 * This class is deprecated in favor of Lumen : https://github.com/Niwer1525/Lumen
 */
@Deprecated(forRemoval = true)
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

		Console.log(handlers);
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
                Console.log("Uncaught exception in thread " + t.getName() +"\n"+ of(e)).error().send();
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
	 * Get the line caller of the current thread
	 * @return The line caller in the format [FileName:LineNumber] + " " (Space at the end)
	 */
	public static String getLineCaller() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace.length > 4) {
			final StackTraceElement caller = stackTrace[4];
			return ANSI_YELLOW+"[" + caller.getFileName() + ":" + caller.getLineNumber() + "] "+ANSI_WHITE;
		}
		return "[Unknown] ";
	}
	
	/**
	 * Display a message in the console
	 * @param b The boolean to display
	 * 
	 * N.B : This method is used to avoid abiguous method call with the other debug methods
	 */
	public static void debug(boolean b) { debug(false, new Object[] {b}); }

	/**
	 * Display a message in the console
	 * @param objects The message to display
	 */
	public static void debug(@Nullable Object... objects) { debug(false, objects); }
	
	public static void debug(boolean newLineOnEachObject, @Nullable Object... objects) {
		System.out.print(getLineCaller());
		System.out.print(ANSI_CYAN+"[" + java.time.LocalTime.now().withNano(0) + "] "+ANSI_WHITE);
		if(objects != null) {
			for(Object o : objects) System.out.print(o + (newLineOnEachObject ? "\n" : " "));
		} else System.out.print("null");
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
			Console.log(message+" ("+(System.currentTimeMillis()-startTime)+"ms)").send();
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
			throw new UnsupportedOperationException("Deprecated. Use Lumen instead : https://github.com/Niwer1525/Lumen");
			// final String subTypeName = (isError?" : "+ConsoleManager.ANSI_RED+"ERROR"+type.consoleColor:"");
			
			// if(container !=null) {
			// 	/* Changing log format */
			// 	if(!(container.getHandler().getFormatter() instanceof ConsoleFormatter)) container.getHandler().setFormatter(new ConsoleFormatter(type));
				
			// 	/* Log */
			// 	container.getLogger().log(Level.OFF, "["+type+subTypeName+"] "+object);
			// } else {
			// 	/* Changing log format */
			// 	if(!(defaultHandler.getFormatter() instanceof ConsoleFormatter)) defaultHandler.setFormatter(new ConsoleFormatter(type));
	
			// 	/* Log */
			// 	defaultLogger.log(Level.OFF, "["+type+subTypeName+"] "+object);
			// }

			// /* Display the error on discord if enabled */
			// if(logOnDiscord) {
			// 	/* If on network -> Don't send packets */
			// 	if(BotEngine.isBotInitialized()) BotEngine.log(isError? Color.RED :type.color, type+subTypeName, object, file);
			// 	else {
			// 		final ClientRequestSendDiscordLogs request = new ClientRequestSendDiscordLogs(type, subTypeName, object, file);
			// 		ClientLinkManager.sendTCP(request);
			// 	}
			// }
		}

		public Log withContainer(ConsoleContainer container) {
			this.container = container;
			return this;
		}

		public Log sendToProcessor() {
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
		SQL(new Color(178, 63, 63), ANSI_RED),
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
package com.photon.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private static final ConsoleHandler consoleHandler = new ConsoleHandler();
	private static final Logger logger = Logger.getLogger("");
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
		for (Handler handler : handlers) { rootLogger.removeHandler(handler); }
		
		/* Adding console handler */
		logger.addHandler(consoleHandler);
	}
	
	public static String of(Exception e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static void registerFileHandler(File file) {
		/* Adding file handler */
		try {
    		final FileHandler fh = new FileHandler(file.getPath());
            logger.addHandler(fh);
            fh.setFormatter(new ConsoleFormatter(null));

			/* Save file after closing launcher */
			Runtime.getRuntime().addShutdownHook(new Thread(() -> { savePreviousFile(file); }));
        } catch (SecurityException | IOException e) {}
	}
	
	private static void savePreviousFile(File file) {
		try {
			Files.copy(Path.of(file.getPath()), Path.of(new File(file.getParent(), "launcher-"+PhotonEngine.getDate(true)+".log").getPath()));
		} catch (IOException e) {}
	}

	public static Log create(Object obj) { return new Log().withObject(obj); }

    @Deprecated
	public static void print(Object o) { print(EnumLogType.INFO, false, o); }
	
    @Deprecated
	public static void print(EnumLogType type, Object o) { print(type, false, o); }
	
    @Deprecated
	public static void print(EnumLogType type, boolean discordLog, Object o) { printLine(type, "", discordLog, o); }
	
	public static class Log {
		private EnumLogType type = EnumLogType.INFO;
		private boolean isError = false;
		private boolean logOnDiscord = false;
		private Object object;
		private File file;

		public void end() {
			final String subTypeName = (isError?" : "+ConsoleManager.ANSI_RED+"ERROR"+type.consoleColor:"");

			/* Changing log format */
			consoleHandler.setFormatter(new ConsoleFormatter(type));

			/* Log */
			logger.log(Level.OFF, "["+type+subTypeName+"] "+object);

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

	@Deprecated
	private static void printLine(EnumLogType type, String subType, boolean discordLog, Object o) {
		consoleHandler.setFormatter(new ConsoleFormatter(type)); /* Set log format */
		logger.log(Level.OFF, "["+type+subType+"] " + o);
		if(discordLog == true) {
			final ClientRequestSendDiscordLogs request = new ClientRequestSendDiscordLogs();
			request.type = type;
			request.subType = subType;
			request.content = o;
			NetworkConnectionClient.sendTCP(request);
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
            if(type !=null) sb.append(type.consoleColor);
            sb.append(record.getMessage());
            sb.append("\n");
			if(type !=null) sb.append(ANSI_WHITE);
            return sb.toString();
        }
    }
}
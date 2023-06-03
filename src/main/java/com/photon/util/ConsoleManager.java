package com.photon.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.photon.discord.DiscordEngine;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.messages.requests.ClientRequestSendDiscordLogs;

public class ConsoleManager
{  	
	private static Logger logger = Logger.getLogger("");
	protected static final String ANSI_RESET = "\u001B[0m";
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
		ConsoleHandler consoleHandler = new ConsoleHandler();
		logger.addHandler(consoleHandler);
		consoleHandler.setFormatter(new ConsoleFormatter());
	}
	
	public static void registerFileHandler(File file) {
		/* Adding file handler */
		try {
    		final FileHandler fh = new FileHandler(file.getPath());
            logger.addHandler(fh);
            fh.setFormatter(new ConsoleFormatter());
        } catch (SecurityException | IOException e) {}
	}
	
	public static void print(Object o) { print(EnumLogType.INFO, false, o); }
	
	public static void print(EnumLogType type, Object o) { print(type, false, o); }
	
	public static void print(EnumLogType type, boolean discordLog, Object o) { printLine(type, "", discordLog, o); }

	public static void printDebug(Object o) { printDebug(EnumLogType.INFO, o, false); }
	
	public static void printDebug(EnumLogType type, Object... o) { printDebug(type, false, o); }
	
	public static void printDebug(EnumLogType type, final boolean discordLog, Object o) { printLine(type, " : DEBUG", discordLog, o); }
	
	public static void printError(Object o) { printError(EnumLogType.INFO, false, o); }
	
	public static void printError(EnumLogType type, Object... o) { printError(type, false, o); }
	
	public static void printError(EnumLogType type, final boolean discordLog, Object o) { printLine(type, " : ERROR", discordLog, o); }
	
	private static void printLine(EnumLogType type, String subType, boolean discordLog, Object o) {
		logger.log(Level.OFF, "["+type+subType+"] " + o);
		if(discordLog == true) {
			final ClientRequestSendDiscordLogs request = new ClientRequestSendDiscordLogs();
			request.type = type;
			request.subType = subType;
			request.content = o;
			NetworkConnectionClient.sendTCP(request);
			if(DiscordEngine.jda !=null) DiscordEngine.log(type.color, type+subType, o);
		}
	}
	
	public static enum EnumLogType {
		INFO(new Color(52, 148, 196)), NETWORK(new Color(178, 63, 63)), LAUNCHER(new Color(98, 164, 83)), ANTICHEAT(new Color(196, 148, 52)), CLIENT(new Color(98, 164, 83)), SERVER(new Color(98, 164, 83));
		
		public Color color;
		
		private EnumLogType(Color color) { this.color = color; }
	}
	
	private static class ConsoleFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
            sb.append(ANSI_CYAN);
            sb.append(record.getMessage());
            sb.append("\n"+ANSI_RESET);
            return sb.toString();
        }
         
    }
}
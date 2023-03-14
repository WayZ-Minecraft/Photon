package com.photon.util;

import java.awt.Color;

import com.photon.discord.DiscordEngine;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.messages.requests.ClientRequestSendDiscordLogs;

public class ConsoleManager
{  	
	public static void printDebug(Object o) { printDebug(EnumLogType.INFO, o, false); }
	
	public static void printDebug(EnumLogType type, Object... o) { printDebug(type, false, o); }
	
	public static void printDebug(EnumLogType type, final boolean discordLog, Object o) { printLine(type, " : DEBUG", discordLog, o); }
	
	public static void printError(Object o) { printError(EnumLogType.INFO, false, o); }
	
	public static void printError(EnumLogType type, Object... o) { printError(type, false, o); }
	
	public static void printError(EnumLogType type, final boolean discordLog, Object o) { printLine(type, " : ERROR", discordLog, o); }
	
	public static void print(Object o) { print(EnumLogType.INFO, false, o); }
	
	public static void print(EnumLogType type, Object o) { print(type, false, o); }
	
	public static void print(EnumLogType type, boolean discordLog, Object o) { printLine(type, "", discordLog, o); }      
	
	private static void printLine(EnumLogType type, String subType, boolean discordLog, Object o) {
		System.out.println("["+type+subType+"] " + o);
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
}
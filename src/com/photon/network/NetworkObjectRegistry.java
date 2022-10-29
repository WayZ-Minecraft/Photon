package com.photon.network;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.esotericsoftware.kryo.Kryo;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.photon.network.NetworkDirectories.NetworkConfig;
import com.photon.network.listeners.INetworkMessageListener;
import com.photon.network.messages.requests.ClientRequestAddClass;
import com.photon.network.messages.requests.ClientRequestAddListener;
import com.photon.network.messages.requests.ClientRequestAnticheat;
import com.photon.network.messages.requests.ClientRequestCrashReport;
import com.photon.network.messages.requests.ClientRequestNetworkConfig;
import com.photon.network.messages.requests.ClientRequestRegisterConnection;
import com.photon.network.messages.requests.ClientRequestSendDiscordLogs;
import com.photon.network.messages.requests.account.ClientRequestAccount;
import com.photon.network.messages.requests.account.ClientRequestAccountCreation;
import com.photon.network.messages.requests.account.ClientRequestAccountVerification;
import com.photon.network.messages.requests.news.ClientRequestNewsList;
import com.photon.network.messages.requests.server.ClientRequestAddServer;
import com.photon.network.messages.requests.server.ClientRequestServerList;
import com.photon.network.messages.response.ServerResponseNetworkConfig;
import com.photon.network.messages.response.ServerResponseNewsList;
import com.photon.network.messages.response.ServerResponseServerList;
import com.photon.network.messages.response.account.ServerResponseAccount;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.ObjectNews;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ObjectServer;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class NetworkObjectRegistry
{
	public static Kryo kryo;
	private static ArrayList<Class<?>> classList = new ArrayList<>();
	private static ByteArrayClassLoader loader = new ByteArrayClassLoader();
	
	public static void addClass(String name, byte[] bytes) {
		if(bytes == null) { return; }
		Class<?> clazz = loader.findClass(name, bytes);
		
		if(clazz !=null) {
			if(!classList.contains(clazz)) {
				classList.add(clazz);
				if(kryo !=null) kryo.register(clazz);
			}
		}
	}
	
	public static void addClass(Class<?> type) {
		if(type == null) { return; }
		if(!classList.contains(type)) {
			classList.add(type);
			if(kryo !=null) kryo.register(type);
		}
		if(NetworkConnectionClient.client !=null) {
			final ClientRequestAddClass packet = new ClientRequestAddClass();
			packet.name = type.getName();
			try
			{
				final InputStream iStream = type.getClassLoader().getResourceAsStream(packet.name.replace('.', '/') + ".class");
				packet.bytes = IOUtils.toByteArray(iStream);
				iStream.close();
			} catch(IOException e) {}
			NetworkConnectionClient.client.sendTCP(packet);
		}
	}
		
	public static void load(final Kryo kryo) {
		if(NetworkObjectRegistry.kryo == null) NetworkObjectRegistry.kryo = kryo;
        kryo.register(String.class);
        kryo.register(String[].class);
        kryo.register(Integer.TYPE);
        kryo.register(Long.TYPE);
        kryo.register(ArrayList.class);
        kryo.register(UUID.class);
        kryo.register(Iterator.class);
        kryo.register(JsonDeserializationContext.class);
        kryo.register(Map.Entry.class);
        kryo.register(Map.class);
        kryo.register(JsonElement.class);
        kryo.register(Date.class);
        kryo.register(Boolean.TYPE);
        kryo.register(Boolean.class);
        kryo.register(Color.class);
        kryo.register(Byte.class);
        kryo.register(byte[].class);
        kryo.register(byte.class);
        
        kryo.register(ObjectPlayerAccount.class);
        kryo.register(ClientRequestAccount.class);
        kryo.register(ClientRequestAccountVerification.class);
        kryo.register(ClientRequestAccountCreation.class);
        kryo.register(ServerResponseValidAccount.class);
        kryo.register(ServerResponseAccount.class);
        
        kryo.register(EnumLogType.class);
        kryo.register(ClientRequestSendDiscordLogs.class);
        kryo.register(ClientRequestCrashReport.class);
        kryo.register(ClientRequestAnticheat.class);

        kryo.register(ObjectNews.class);
        kryo.register(ClientRequestNewsList.class);
        kryo.register(ServerResponseNewsList.class);
        
        kryo.register(ObjectServer.class);
        kryo.register(ClientRequestAddServer.class);
        kryo.register(ClientRequestServerList.class);
        kryo.register(ServerResponseServerList.class);
        
        kryo.register(NetworkConfig.class);
        kryo.register(ClientRequestNetworkConfig.class);
        kryo.register(ServerResponseNetworkConfig.class);
        kryo.register(ClientRequestRegisterConnection.class);
        
        kryo.register(ClientRequestAddClass.class);
        kryo.register(INetworkMessageListener.class);
        kryo.register(ClientRequestAddListener.class);
    }
	
	public static class ByteArrayClassLoader extends ClassLoader {
		
		private static HashMap<String, Class<?>> list = new HashMap<>();
		
		public Class<?> findClass(String name, byte[] bytes) {
			if(!list.containsKey(name)) {				
				try {
					Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
					list.put(name, clazz);
					return clazz;
				} catch(ClassFormatError e) { ConsoleManager.print("Error : " + e.fillInStackTrace().toString()); }
			}
			return list.get(name);
		}
	}
}

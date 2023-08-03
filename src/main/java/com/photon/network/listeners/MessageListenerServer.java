package com.photon.network.listeners;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.PhotonEngine;
import com.photon.discord.BotEngine;
import com.photon.network.NetworkConnectionServer;
import com.photon.network.NetworkDirectories;
import com.photon.network.NetworkObjectRegistry;
import com.photon.network.listeners.INetworkMessageListener.INetworkListenerSide;
import com.photon.network.messages.requests.ClientRequestAddClass;
import com.photon.network.messages.requests.ClientRequestAddListener;
import com.photon.network.messages.requests.ClientRequestAnticheat;
import com.photon.network.messages.requests.ClientRequestCrashReport;
import com.photon.network.messages.requests.ClientRequestNetworkConfig;
import com.photon.network.messages.requests.ClientRequestRegisterConnection;
import com.photon.network.messages.requests.ClientRequestSendDiscordLogs;
import com.photon.network.messages.requests.ClientRequestSyncContentPacks;
import com.photon.network.messages.requests.account.ClientRequestAccount;
import com.photon.network.messages.requests.account.ClientRequestAccountCreation;
import com.photon.network.messages.requests.account.ClientRequestAccountVerification;
import com.photon.network.messages.requests.news.ClientRequestNewsList;
import com.photon.network.messages.requests.server.ClientRequestAddServer;
import com.photon.network.messages.requests.server.ClientRequestServerList;
import com.photon.network.messages.response.ServerResponseNetworkConfig;
import com.photon.network.messages.response.ServerResponseNewsList;
import com.photon.network.messages.response.ServerResponseServerList;
import com.photon.network.messages.response.ServerResponseSyncContentPack;
import com.photon.network.messages.response.account.ServerResponseAccount;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.DownloadContentPacks;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ObjectServer;
import com.photon.network.objects.ProfileManager;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class MessageListenerServer implements Listener
{
    @Override
    public void received(final Connection connection, final Object object) {
    	try {
    		if (object instanceof ClientRequestRegisterConnection request) {
    			PhotonEngine.networkConnectionsList.remove(request.uuid);
    			PhotonEngine.networkConnectionsList.put(request.uuid, connection);
    		}
    		else if (object instanceof ClientRequestAccount request) {
				final ServerResponseAccount response = new ServerResponseAccount();
	            if (request.UUID != null) response.givenProfile = ProfileManager.getProfileFromUUID(request.UUID);
	            if (request.email != null) response.givenProfile = ProfileManager.getProfileFromEMail(request.email);
	            if (request.discordID != null) response.givenProfile = ProfileManager.getProfileFromDiscordID(request.discordID);
				connection.sendTCP(response);
	        }
	        else if (object instanceof ClientRequestAccountVerification request) {
	        	final ServerResponseValidAccount response = new ServerResponseValidAccount();
	        	final ObjectPlayerAccount profile = ProfileManager.getProfileFromEMail(request.email);
	        	if(response.exist = profile != null) {
	        		response.isValidPassword = profile.password.equals(request.password);
	        		if(response.isValidPassword) response.profile = profile;
	        	}
	        	connection.sendTCP(response);
	        }
	        else if (object instanceof ClientRequestAccountCreation request) {
	        	final ServerResponseValidAccount response = new ServerResponseValidAccount();
	        	if(ProfileManager.doesProfileExistByEMail(request.email)) { response.isEmailAlreadyUsed = true; return; }
	    		if(ProfileManager.doesProfileExistByUsername(request.username)) { response.isUsernameAlreadyUsed = true; return; }
	        	final ObjectPlayerAccount profile = ProfileManager.createPlayerProfile(request.username, request.email, request.password);
	        	if(response.exist = profile != null) {
	        		response.profile = profile;
                    ConsoleManager.create("A new user created an account! (" + profile.username + ")").displayOnDiscord().withType(EnumLogType.NETWORK).end();
	        	}
	        	connection.sendTCP(response);
	        }
			
	        else if (object instanceof ClientRequestSyncContentPacks request) {
				for(ObjectServer server : PhotonEngine.networkServerList) {
					if(server.serverIP.equalsIgnoreCase(request.ip) && server.serverPort == request.port) {
						final ServerResponseSyncContentPack response = new ServerResponseSyncContentPack();
						response.connectionID = connection.getID();
						response.sha1 = request.sha1;
						response.filesCount = request.filesCount;
						NetworkConnectionServer.server.sendToTCP(server.connectionID, response);
						break;
					}
				}
			}
	        else if (object instanceof DownloadContentPacks request) NetworkConnectionServer.server.sendToTCP(request.connectionID, request);

	        else if (object instanceof ClientRequestCrashReport request) {
	            final File crashReportFolder = new File(NetworkDirectories.crashDirectory, request.userUUID);
	            final File crashReportFile = new File(crashReportFolder, request.fileName + ".txt");
	            if (!crashReportFolder.exists()) crashReportFolder.mkdirs();
	            if (!crashReportFile.exists()) crashReportFile.createNewFile();
	            final BufferedWriter writer = new BufferedWriter(new FileWriter(crashReportFile));
	            writer.write(request.fileMessage);
	            writer.close();
                ConsoleManager.create("Received a new Crash Report from Client: " + request.userUUID).displayOnDiscord().withType(EnumLogType.NETWORK).end();
	        }
	        
	        else if (object instanceof ClientRequestAnticheat request) {
	            final File anticheatFolder = new File(NetworkDirectories.anticheatDirectory, request.userUUID);
	            final File anticheatFile = new File(anticheatFolder, request.fileName + ".txt");
	            if (!anticheatFolder.exists()) anticheatFolder.mkdirs();
	            if (!anticheatFile.exists()) anticheatFile.createNewFile();
	            final BufferedWriter writer = new BufferedWriter(new FileWriter(anticheatFile));
	            writer.write(request.fileMessage);
	            writer.write("\nOperating System: " + request.operatingSystem);
	            writer.close();
                ConsoleManager.create("Cheater: "+request.userUUID+"\nOperating System: "+request.operatingSystem + "\nReason: " + request.fileMessage).displayOnDiscord().withType(EnumLogType.NETWORK).end();
	        }
    		
	        else if (object instanceof ClientRequestNewsList) {
	            final ServerResponseNewsList response = new ServerResponseNewsList();
	            response.newsObjects = PhotonEngine.networkNewsList;
	            connection.sendTCP(response);
	        }
	        
	        else if (object instanceof ClientRequestSendDiscordLogs request && BotEngine.botBuilder !=null) {
	        	BotEngine.log(request.type.color, request.type+" : "+request.subType, request.content, request.file);
	        }
	        
	        else if (object instanceof ClientRequestAddServer request) {
	            boolean cancelAdding = false;
	            for (final ObjectServer server : PhotonEngine.networkServerList) {
	                if (server.serverIP.equals(request.objServer.serverIP) && server.serverPort == request.objServer.serverPort) {
	                    cancelAdding = true;
	                    if (!request.objServer.serverMOTD.equals(server.serverMOTD)) server.serverMOTD = request.objServer.serverMOTD;
						server.connectionID = connection.getID();
	                }
	            }
	            if (!cancelAdding) {
					request.objServer.connectionID = connection.getID();
	            	PhotonEngine.networkServerList.add(request.objServer);
                    ConsoleManager.create("Adding server to the server list: "+request.objServer.serverIP+":"+request.objServer.serverPort+"\nMOTD: "
                        +request.objServer.serverMOTD + "\nName: " + request.objServer.serverName).displayOnDiscord().withType(EnumLogType.NETWORK).end();
	            }
	        }
	        else if (object instanceof ClientRequestServerList) {
	            final ServerResponseServerList response = new ServerResponseServerList();
	            response.serverObjects = PhotonEngine.networkServerList;
	            connection.sendTCP(response);
	        }
	        
	        else if (object instanceof ClientRequestNetworkConfig) {
	            final ServerResponseNetworkConfig response = new ServerResponseNetworkConfig();
	            response.config = NetworkDirectories.config;
	            connection.sendTCP(response);
	        } 
	        
	        else if (object instanceof ClientRequestAddClass request) NetworkObjectRegistry.addClass(request.name, request.bytes);
	        else if (object instanceof ClientRequestAddListener request) MessageListenerCommon.addListener(request.listener);
    	} catch (IOException e) { e.printStackTrace(); }
    	
    	for(INetworkMessageListener listener : MessageListenerCommon.listeners) {
        	if(listener.useOn() == INetworkListenerSide.SERVER) listener.received(connection, object);
        }
    }
}

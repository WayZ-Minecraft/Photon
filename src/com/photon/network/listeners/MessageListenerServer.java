package com.photon.network.listeners;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.photon.PhotonEngine;
import com.photon.discord.DiscordEngine;
import com.photon.network.NetworkDirectories;
import com.photon.network.NetworkObjectRegistry;
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
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ObjectServer;
import com.photon.network.objects.ProfileManager;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class MessageListenerServer extends Listener
{
    @Override
    public void received(final Connection connection, final Object object) {
    	try {
    		if (object instanceof ClientRequestRegisterConnection) {
    			final ClientRequestRegisterConnection request = (ClientRequestRegisterConnection)object;
    			PhotonEngine.networkConnectionsList.remove(request.uuid);
    			PhotonEngine.networkConnectionsList.put(request.uuid, connection);
    		}
    		else if (object instanceof ClientRequestAccount) { //TODO WTF
	            final ClientRequestAccount request = (ClientRequestAccount)object;
	            if (request.UUID != null) {}
	            if (request.email != null) {}
	            final ServerResponseAccount response = new ServerResponseAccount();
	            response.givenProfile = ProfileManager.getProfileFromUUID(request.UUID);
	            connection.sendTCP(response);
	        }
	        else if (object instanceof ClientRequestAccountVerification) {
	        	final ClientRequestAccountVerification request = (ClientRequestAccountVerification)object;
	        	final ServerResponseValidAccount response = new ServerResponseValidAccount();
	        	final ObjectPlayerAccount profile = ProfileManager.getProfileFromEMail(request.email);
	        	if(response.exist = profile != null) {
	        		response.isValidPassword = profile.password.equals(request.password);
	        		if(response.isValidPassword) { response.profile = profile; }
	        	}
	        	connection.sendTCP(response);
	        }
	        else if (object instanceof ClientRequestAccountCreation) {
	        	final ClientRequestAccountCreation request = (ClientRequestAccountCreation)object;
	        	final ServerResponseValidAccount response = new ServerResponseValidAccount();
	        	if(ProfileManager.doesProfileExistByEMail(request.email)) { response.isEmailAlreadyUsed = true; return; }
	    		if(ProfileManager.doesProfileExistByUsername(request.username)) { response.isUsernameAlreadyUsed = true; return; }
	        	final ObjectPlayerAccount profile = ProfileManager.createPlayerProfile(request.username, request.email, request.password);
	        	if(response.exist = profile != null) {
	        		response.profile = profile;
	        		ConsoleManager.print(EnumLogType.NETWORK, true, "A new user created an account! (" + profile.username + ")");
	        	}
	        	connection.sendTCP(response);
	        }
	        
	        else if (object instanceof ClientRequestCrashReport) {
	            final ClientRequestCrashReport request = (ClientRequestCrashReport)object;
	            final File crashReportFolder = new File(NetworkDirectories.crashDirectory, request.userUUID);
	            final File crashReportFile = new File(crashReportFolder, request.fileName + ".txt");
	            if (!crashReportFolder.exists()) crashReportFolder.mkdirs();
	            if (!crashReportFile.exists()) crashReportFile.createNewFile();
	            final BufferedWriter writer = new BufferedWriter(new FileWriter(crashReportFile));
	            writer.write(request.fileMessage);
	            writer.close();
	            ConsoleManager.print(EnumLogType.NETWORK, true, "Received a new Crash Report from Client: " + request.userUUID);
	        }
	        
	        else if (object instanceof ClientRequestAnticheat) {
	            final ClientRequestAnticheat request = (ClientRequestAnticheat)object;
	            final File anticheatFolder = new File(NetworkDirectories.anticheatDirectory, request.userUUID);
	            final File anticheatFile = new File(anticheatFolder, request.fileName + ".txt");
	            if (!anticheatFolder.exists()) anticheatFolder.mkdirs();
	            if (!anticheatFile.exists()) anticheatFile.createNewFile();
	            final BufferedWriter writer = new BufferedWriter(new FileWriter(anticheatFile));
	            writer.write(request.fileMessage);
	            writer.write("\nOperating System: " + request.operatingSystem);
	            writer.close();
	            ConsoleManager.print(EnumLogType.NETWORK, true, "Cheater: "+request.userUUID+"\nOperating System: "+request.operatingSystem + "\nReason: " + request.fileMessage);
	        }
    		
	        else if (object instanceof ClientRequestNewsList) {
	            final ServerResponseNewsList response = new ServerResponseNewsList();
	            response.newsObjects = PhotonEngine.networkNewsList;
	            connection.sendTCP(response);
	        }
	        
	        else if (object instanceof ClientRequestSendDiscordLogs && DiscordEngine.jda !=null) {
	        	ClientRequestSendDiscordLogs request = (ClientRequestSendDiscordLogs)object;
	        	DiscordEngine.log(request.type.color, request.type+" : "+request.subType, request.content);
	        }
	        
	        else if (object instanceof ClientRequestAddServer) {
	        	final ClientRequestAddServer request = (ClientRequestAddServer)object;
	            boolean cancelAdding = false;
	            for (final ObjectServer server : PhotonEngine.networkServerList) {
	                if (server.serverIP.equals(request.objServer.serverIP) && server.serverPort == request.objServer.serverPort) {
	                    cancelAdding = true;
	                    if (!request.objServer.serverMOTD.equals(server.serverMOTD)) { server.serverMOTD = request.objServer.serverMOTD; }
	                }
	            }
	            if (!cancelAdding) {
	            	PhotonEngine.networkServerList.add(request.objServer);
	            	ConsoleManager.print(EnumLogType.NETWORK, "Adding server to the server list: "+request.objServer.serverIP+":"+request.objServer.serverPort+"\nMOTD: "+request.objServer.serverMOTD + "\nName: " + request.objServer.serverName);
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
	        
	        else if (object instanceof ClientRequestAddClass) NetworkObjectRegistry.addClass(((ClientRequestAddClass)object).name, ((ClientRequestAddClass)object).bytes);
	        else if (object instanceof ClientRequestAddListener) MessageListenerCommon.addListener(((ClientRequestAddListener)object).listener);
    	} catch (IOException e) {}
    	
    	for(INetworkMessageListener listener : MessageListenerCommon.listeners) {
        	if(listener.serverSide()) listener.received(connection, object);
        }
    }
}

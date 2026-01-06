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
import com.photon.network.messages.requests.ClientRequestHWID;
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
import com.photon.network.objects.ObjectHWIDs;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ObjectServer;
import com.photon.network.objects.ProfileManager;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

public class MessageListenerServer implements Listener {
    
    @Override
    public void received(Connection connection, Object object) {
        try {
            handleCoreRequests(connection, object);
            MessageListenerCommon.dispatchToListeners(connection, object, INetworkListenerSide.SERVER);
        } catch (IOException e) {
            ConsoleManager.create("IO error while handling: " + object.getClass().getSimpleName())
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            e.printStackTrace();
        } catch (Exception e) {
            ConsoleManager.create("Unexpected error while handling: " + object.getClass().getSimpleName())
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            e.printStackTrace();
        }
    }
    
    private void handleCoreRequests(Connection connection, Object object) throws IOException {
        if (object instanceof ClientRequestRegisterConnection request) {
            handleConnectionRegistration(connection, request);
        }
        else if (object instanceof ClientRequestAccount request) {
            handleAccountRequest(connection, request);
        }
        else if (object instanceof ClientRequestAccountVerification request) {
            handleAccountVerification(connection, request);
        }
        else if (object instanceof ClientRequestAccountCreation request) {
            handleAccountCreation(connection, request);
        }
        else if (object instanceof ClientRequestSyncContentPacks request) {
            handleContentPackSync(request);
        }
        else if (object instanceof DownloadContentPacks request) {
            handleContentPackDownload(request);
        }
        else if (object instanceof ClientRequestCrashReport request) {
            handleCrashReport(request);
        }
        else if (object instanceof ClientRequestAnticheat request) {
            handleAnticheatReport(request);
        }
        else if (object instanceof ClientRequestHWID request) {
            handleHWIDReport(request);
        }
        else if (object instanceof ClientRequestNewsList) {
            handleNewsListRequest(connection);
        }
        else if (object instanceof ClientRequestSendDiscordLogs request) {
            handleDiscordLog(request);
        }
        else if (object instanceof ClientRequestAddServer request) {
            handleServerRegistration(connection, request);
        }
        else if (object instanceof ClientRequestServerList) {
            handleServerListRequest(connection);
        }
        else if (object instanceof ClientRequestNetworkConfig) {
            handleNetworkConfigRequest(connection);
        }
        else if (object instanceof ClientRequestAddClass request) {
            handleClassRegistration(request);
        }
        else if (object instanceof ClientRequestAddListener request) {
            handleListenerRegistration(request);
        }
    }
    
    private void handleConnectionRegistration(Connection connection, ClientRequestRegisterConnection request) {
        PhotonEngine.networkConnectionsList.remove(request.getUuid());
        PhotonEngine.networkConnectionsList.put(request.getUuid(), connection);
        
        ConsoleManager.create("Connection registered: " + request.getUuid())
            .withType(EnumLogType.NETWORK)
            .end();
    }
    
    private void handleAccountRequest(Connection connection, ClientRequestAccount request) {
        ObjectPlayerAccount givenProfile = null;
        
        if (request.getUUID() != null) {
            givenProfile = ProfileManager.getProfileFromUUID(request.getUUID());
        } else if (request.getEmail() != null) {
            givenProfile = ProfileManager.getProfileFromEMail(request.getEmail());
        } else if (request.getDiscordID() != null) {
            givenProfile = ProfileManager.getProfileFromDiscordID(request.getDiscordID());
        }
        
        ServerResponseAccount response = new ServerResponseAccount(givenProfile);
        connection.sendTCP(response);
    }
    
    private void handleAccountVerification(Connection connection, ClientRequestAccountVerification request) {
        ObjectPlayerAccount profile = ProfileManager.getProfileFromEMail(request.getEmail());
        
        boolean exist = (profile != null);
        boolean isValidPassword = false;
        ObjectPlayerAccount returnProfile = null;
        
        if (exist) {
            isValidPassword = profile.password.equals(request.getPassword());
            if (isValidPassword) {
                returnProfile = profile;
            }
        }
        
        ServerResponseValidAccount response = new ServerResponseValidAccount(
            exist, isValidPassword, false, false, false, returnProfile
        );
        connection.sendTCP(response);
    }
    
    private void handleAccountCreation(Connection connection, ClientRequestAccountCreation request) {
        boolean isEmailAlreadyUsed = ProfileManager.doesProfileExistByEMail(request.getEmail());
        boolean isUsernameAlreadyUsed = ProfileManager.doesProfileExistByUsername(request.getUsername());
        
        boolean exist = false;
        ObjectPlayerAccount profile = null;
        
        if (!isEmailAlreadyUsed && !isUsernameAlreadyUsed) {
            profile = ProfileManager.createPlayerProfile(
                request.getUsername(), 
                request.getEmail(), 
                request.getPassword()
            );
            
            exist = (profile != null);
            
            if (exist) {
                ConsoleManager.create("New account created: " + profile.username)
                    .displayOnDiscord()
                    .withType(EnumLogType.NETWORK)
                    .end();
            }
        }
        
        ServerResponseValidAccount response = new ServerResponseValidAccount(
            exist, false, isEmailAlreadyUsed, isUsernameAlreadyUsed, false, profile
        );
        connection.sendTCP(response);
    }
    
    private void handleContentPackSync(ClientRequestSyncContentPacks request) {
        for (ObjectServer server : PhotonEngine.networkServerList) {
            if (server.serverIP.equalsIgnoreCase(request.getIp()) && server.serverPort == request.getPort()) {
                ServerResponseSyncContentPack response = new ServerResponseSyncContentPack(
                    server.connectionID,
                    request.getFilesCount(),
                    request.getSha1()
                );
                
                NetworkConnectionServer.server.sendToTCP(server.connectionID, response);
                break;
            }
        }
    }
    
    private void handleContentPackDownload(DownloadContentPacks request) {
        NetworkConnectionServer.server.sendToTCP(request.connectionID, request);
    }
    
    private void handleCrashReport(ClientRequestCrashReport request) throws IOException {
        File crashReportFolder = new File(NetworkDirectories.crashDirectory, request.getUserUUID());
        File crashReportFile = new File(crashReportFolder, request.getFileName() + ".txt");
        
        if (!crashReportFolder.exists()) {
            crashReportFolder.mkdirs();
        }
        
        if (!crashReportFile.exists()) {
            crashReportFile.createNewFile();
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(crashReportFile))) {
            writer.write(request.getFileMessage());
        }
        
        ConsoleManager.create("Crash report received: " + request.getUserUUID())
            .displayOnDiscord()
            .withType(EnumLogType.NETWORK)
            .withFile(crashReportFile)
            .end();
    }
    
    private void handleAnticheatReport(ClientRequestAnticheat request) throws IOException {
        File anticheatFolder = new File(NetworkDirectories.anticheatDirectory, request.getUserUUID());
        File anticheatFile = new File(anticheatFolder, request.getFileName() + ".txt");
        
        if (!anticheatFolder.exists()) {
            anticheatFolder.mkdirs();
        }
        
        if (!anticheatFile.exists()) {
            anticheatFile.createNewFile();
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(anticheatFile))) {
            writer.write(request.getFileMessage());
            writer.write("\nOperating System: " + request.getOperatingSystem());
        }
        
        ConsoleManager.create("Cheater detected: " + request.getUserUUID() + 
            "\nOS: " + request.getOperatingSystem() + 
            "\nReason: " + request.getFileMessage())
            .displayOnDiscord()
            .withType(EnumLogType.NETWORK)
            .end();
    }
    
    private void handleHWIDReport(ClientRequestHWID request) throws IOException {
        File HWIDsFile = new File(NetworkDirectories.baseDirectory, "HWIDs.json");
        
        if (!HWIDsFile.exists()) {
            HWIDsFile.createNewFile();
            ObjectHWIDs.create(HWIDsFile);
        }
        
        ObjectHWIDs hwids = ObjectHWIDs.load(HWIDsFile);
        if (hwids == null) {
            hwids = new ObjectHWIDs();
        }
        
        hwids.hwids.add(new ObjectHWIDs.HWID(
            request.getUserName(), 
            request.getUserUUID(), 
            request.getUserHWID(), 
            request.getOperatingSystem()
        ));
        
        hwids.save(HWIDsFile);
        
        ConsoleManager.create("HWID received: " + request.getUserUUID() + 
            "\nOS: " + request.getOperatingSystem() + 
            "\nHWID: " + request.getUserHWID())
            .withType(EnumLogType.NETWORK)
            .end();
    }
    
    private void handleNewsListRequest(Connection connection) {
        ServerResponseNewsList response = new ServerResponseNewsList(PhotonEngine.networkNewsList);
        connection.sendTCP(response);
    }
    
    private void handleDiscordLog(ClientRequestSendDiscordLogs request) {
        if (BotEngine.botBuilder != null) {
            BotEngine.log(
                request.getType().color, 
                request.getType() + " : " + request.getSubType(), 
                request.getContent(), 
                request.getFile()
            );
        }
    }
    
    private void handleServerRegistration(Connection connection, ClientRequestAddServer request) {
        ObjectServer clientServer = request.getObjServer();
        
        // SECURITY: Verify IP matches connection
        String clientIP = connection.getRemoteAddressTCP().getAddress().getHostAddress();
        if (!clientIP.equals(clientServer.serverIP)) {
            ConsoleManager.create("Server registration rejected: IP mismatch (claimed: " + 
                clientServer.serverIP + ", actual: " + clientIP + ")")
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            return;
        }
        
        // SECURITY: Validate port range
        if (clientServer.serverPort < 1024 || clientServer.serverPort > 65535) {
            ConsoleManager.create("Server registration rejected: Invalid port " + clientServer.serverPort)
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            return;
        }
        
        // SECURITY: Sanitize strings (basic XSS prevention)
        if (clientServer.serverName == null || clientServer.serverName.isEmpty() || 
            clientServer.serverName.length() > 64 ||
            clientServer.serverName.contains("<") || clientServer.serverName.contains(">")) {
            ConsoleManager.create("Server registration rejected: Invalid server name")
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            return;
        }
        
        if (clientServer.serverMOTD != null && clientServer.serverMOTD.length() > 256) {
            clientServer.serverMOTD = clientServer.serverMOTD.substring(0, 256);
        }
        
        boolean serverExists = false;
        
        for (ObjectServer server : PhotonEngine.networkServerList) {
            if (server.serverIP.equals(clientServer.serverIP) && 
                server.serverPort == clientServer.serverPort) {
                
                serverExists = true;
                
                if (!clientServer.serverMOTD.equals(server.serverMOTD)) {
                    server.serverMOTD = sanitizeString(clientServer.serverMOTD);
                }
                
                server.connectionID = connection.getID();
                break;
            }
        }
        
        if (!serverExists) {
            clientServer.connectionID = connection.getID();
            clientServer.serverName = sanitizeString(clientServer.serverName);
            clientServer.serverMOTD = sanitizeString(clientServer.serverMOTD);
            
            PhotonEngine.networkServerList.add(clientServer);
            
            ConsoleManager.create("Server registered: " + 
                clientServer.serverIP + ":" + clientServer.serverPort + 
                "\nName: " + clientServer.serverName + 
                "\nMOTD: " + clientServer.serverMOTD)
                .displayOnDiscord()
                .withType(EnumLogType.NETWORK)
                .end();
        }
    }
    
    private String sanitizeString(String input) {
        if (input == null) return "";
        return input.replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("&", "&amp;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
    
    private void handleServerListRequest(Connection connection) {
        ServerResponseServerList response = new ServerResponseServerList(PhotonEngine.networkServerList);
        connection.sendTCP(response);
    }
    
    private void handleNetworkConfigRequest(Connection connection) {
        ServerResponseNetworkConfig response = new ServerResponseNetworkConfig(NetworkDirectories.getConfig());
        connection.sendTCP(response);
    }
    
    private void handleClassRegistration(ClientRequestAddClass request) {
        NetworkObjectRegistry.addClass(request.getName(), request.getBytes());
        
        ConsoleManager.create("Network class registered: " + request.getName())
            .withType(EnumLogType.NETWORK)
            .end();
    }
    
    private void handleListenerRegistration(ClientRequestAddListener request) {
        MessageListenerCommon.addListener(request.getListener());
    }
}
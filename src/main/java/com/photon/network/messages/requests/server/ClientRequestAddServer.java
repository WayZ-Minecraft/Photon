package com.photon.network.messages.requests.server;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.NetworkEngine;
import com.photon.network.objects.ObjectServer;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author noz43
 */

public class ClientRequestAddServer {
    private final ObjectServer objServer;
    
    public ClientRequestAddServer(ObjectServer objServer) {
        this.objServer = objServer;
    }
    
    public ObjectServer getObjServer() { return objServer; }
    
    public void handle(Connection connection) {
        String clientIP = connection.getRemoteAddressTCP().getAddress().getHostAddress();
        if (!clientIP.equals(objServer.serverIP)) {
            ConsoleManager.create("Server registration rejected: IP mismatch (claimed: " + 
                objServer.serverIP + ", actual: " + clientIP + ")")
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            return;
        }
        
        if (objServer.serverPort < 1024 || objServer.serverPort > 65535) {
            ConsoleManager.create("Server registration rejected: Invalid port " + objServer.serverPort)
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            return;
        }
        
        if (objServer.serverName == null || objServer.serverName.isEmpty() || 
            objServer.serverName.length() > 64 ||
            objServer.serverName.contains("<") || objServer.serverName.contains(">")) {
            ConsoleManager.create("Server registration rejected: Invalid server name")
                .withType(EnumLogType.NETWORK)
                .error()
                .end();
            return;
        }
        
        if (objServer.serverMOTD != null && objServer.serverMOTD.length() > 256) {
            objServer.serverMOTD = objServer.serverMOTD.substring(0, 256);
        }
        
        boolean serverExists = false;
        
        for (ObjectServer server : NetworkEngine.SAVED_SERVER_LIST) {
            if (server.serverIP.equals(objServer.serverIP) && 
                server.serverPort == objServer.serverPort) {
                
                serverExists = true;
                
                if (!objServer.serverMOTD.equals(server.serverMOTD)) {
                    server.serverMOTD = sanitizeString(objServer.serverMOTD);
                }
                
                server.connectionID = connection.getID();
                break;
            }
        }
        
        if (!serverExists) {
            objServer.connectionID = connection.getID();
            objServer.serverName = sanitizeString(objServer.serverName);
            objServer.serverMOTD = sanitizeString(objServer.serverMOTD);
            
            NetworkEngine.SAVED_SERVER_LIST.add(objServer);
            
            ConsoleManager.create("Server registered: " + 
                objServer.serverIP + ":" + objServer.serverPort + 
                "\nName: " + objServer.serverName + 
                "\nMOTD: " + objServer.serverMOTD)
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
}
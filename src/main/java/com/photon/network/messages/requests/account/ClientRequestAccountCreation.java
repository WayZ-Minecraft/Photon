package com.photon.network.messages.requests.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.IPacket;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.sql.PlayerAccountTable;
import com.photon.util.PhotonLogTypes;

import niwer.lumen.Console;

/**
 * @author Niwer
 * @author noz43
 */

public class ClientRequestAccountCreation implements IPacket {
    private final String username;
    private final String email;
    private final String password;
    
    public ClientRequestAccountCreation() {
        this.username = null;
        this.email = null;
        this.password = null;
    }

    public ClientRequestAccountCreation(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    
    @Override
    public void handle(Connection connection) {
        final boolean isEmailAlreadyUsed = PlayerAccountTable.emailExists(email);
        final boolean isUsernameAlreadyUsed = PlayerAccountTable.usernameExists(username);
        
        boolean exist = false;
        ObjectPlayerAccount profile = null;
        
        if (!isEmailAlreadyUsed && !isUsernameAlreadyUsed) {
            profile = PlayerAccountTable.createAccount(username, email, password);
            exist = profile != null;
            
            if (exist) Console.log("New account created: " + profile.username).sendToProcessor().type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
        }
        
        final ServerResponseValidAccount response = new ServerResponseValidAccount(exist, true, isEmailAlreadyUsed, isUsernameAlreadyUsed, false, profile);
        connection.sendTCP(response);
    }
}
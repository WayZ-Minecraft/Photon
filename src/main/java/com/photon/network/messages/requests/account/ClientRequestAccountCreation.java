package com.photon.network.messages.requests.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

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
        final boolean isEmailAlreadyUsed = SQLPlayerAccount.emailExists(email);
        final boolean isUsernameAlreadyUsed = SQLPlayerAccount.usernameExists(username);
        
        boolean exist = false;
        ObjectPlayerAccount profile = null;
        
        if (!isEmailAlreadyUsed && !isUsernameAlreadyUsed) {
            ConsoleManager.debug("No account with the same email or username. Creating a new one...");
            profile = SQLPlayerAccount.createAccount(username, email, password);
            exist = profile != null;
            
            if (exist) ConsoleManager.create("New account created: " + profile.username).displayOnDiscord().withType(EnumLogType.NETWORK).end();
        }
        
        final ServerResponseValidAccount response = new ServerResponseValidAccount(exist, true, isEmailAlreadyUsed, isUsernameAlreadyUsed, false, profile);
        connection.sendTCP(response);
    }
}
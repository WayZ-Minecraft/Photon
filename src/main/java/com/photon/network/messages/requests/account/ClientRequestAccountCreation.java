package com.photon.network.messages.requests.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.ProfileManager;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author noz43
 */

public class ClientRequestAccountCreation {
    private final String username;
    private final String email;
    private final String password;
    
    public ClientRequestAccountCreation(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    
    public void handle(Connection connection) {
        boolean isEmailAlreadyUsed = ProfileManager.doesProfileExistByEMail(email);
        boolean isUsernameAlreadyUsed = ProfileManager.doesProfileExistByUsername(username);
        
        boolean exist = false;
        ObjectPlayerAccount profile = null;
        
        if (!isEmailAlreadyUsed && !isUsernameAlreadyUsed) {
            profile = ProfileManager.createPlayerProfile(username, email, password);
            
            exist = (profile != null);
            
            if (exist) {
                ConsoleManager.create("New account created: " + profile.username)
                    .displayOnDiscord()
                    .withType(EnumLogType.NETWORK)
                    .end();
            }
        }
        
        ServerResponseValidAccount response = new ServerResponseValidAccount(
            exist, true, isEmailAlreadyUsed, isUsernameAlreadyUsed, false, profile
        );
        connection.sendTCP(response);
    }
}
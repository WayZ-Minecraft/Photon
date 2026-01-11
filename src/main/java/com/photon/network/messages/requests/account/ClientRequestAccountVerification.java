package com.photon.network.messages.requests.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.sql.SQLPlayerAccount;

/**
 * @author Niwer
 * @author noz43
 */

public class ClientRequestAccountVerification implements IPacket {
    private final String email;
    private final String password;
    
    public ClientRequestAccountVerification(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    
    @Override
    public void handle(Connection connection) {
        ObjectPlayerAccount profile = SQLPlayerAccount.getAccountByEmail(email);
        
        boolean exist = (profile != null);
        boolean isValidPassword = false;
        ObjectPlayerAccount returnProfile = null;
        
        if (exist) {
            isValidPassword = profile.password.equals(password);
            if (isValidPassword) {
                returnProfile = profile;
            }
        }
        
        ServerResponseValidAccount response = new ServerResponseValidAccount(
            exist, isValidPassword, false, false, false, returnProfile
        );
        connection.sendTCP(response);
    }
}
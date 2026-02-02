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
    
    public ClientRequestAccountVerification() {
        this.email = "";
        this.password = "";
    }

    public ClientRequestAccountVerification(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    
    @Override
    public void handle(Connection connection) {
        final ObjectPlayerAccount profile = SQLPlayerAccount.getAccountByEmail(email);
        final boolean exist = profile != null;
        
        if (exist) {
            final ServerResponseValidAccount response = new ServerResponseValidAccount(exist, profile.password.equals(password), profile);
            connection.sendTCP(response);
        }
    }
}
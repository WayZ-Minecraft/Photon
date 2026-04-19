package com.photon.network.messages.requests.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.messages.response.account.ServerResponseAccount;
import com.photon.objects.ObjectPlayerAccount;
import com.photon.sql.PlayerAccountTable;

/**
 * @author Niwer
 * @author noz43
 */
public class ClientRequestAccount implements IPacket {
    private final String UUID;
    private final String email;
    private final String discordID;
    
    public ClientRequestAccount() {
        this.UUID = null;
        this.email = null;
        this.discordID = null;
    }

    public ClientRequestAccount(String UUID, String email, String discordID) {
        this.UUID = UUID;
        this.email = email;
        this.discordID = discordID;
    }
    
    public String getUUID() { return UUID; }
    public String getEmail() { return email; }
    public String getDiscordID() { return discordID; }
    
    @Override
    public void handle(Connection connection) {
        ObjectPlayerAccount givenProfile = null;
        
        /* Try to get the profile */
        if (UUID != null) givenProfile = PlayerAccountTable.getAccountByUUID(UUID);
        else if (email != null) givenProfile = PlayerAccountTable.getAccountByEmail(email);
        else if (discordID != null) givenProfile = PlayerAccountTable.getAccountByDiscordID(discordID);
        
        if(givenProfile == null) return; // No account found
        connection.sendTCP(new ServerResponseAccount(givenProfile));
    }
}
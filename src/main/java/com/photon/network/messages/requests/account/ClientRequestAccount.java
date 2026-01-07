package com.photon.network.messages.requests.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.IPacket;
import com.photon.network.ProfileManager;
import com.photon.network.messages.response.account.ServerResponseAccount;
import com.photon.network.objects.ObjectPlayerAccount;

/**
 * @author Niwer
 * @author noz43
 */

public class ClientRequestAccount implements IPacket {
    private final String UUID;
    private final String email;
    private final String discordID;
    
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
        
        if (UUID != null) {
            givenProfile = ProfileManager.getProfileFromUUID(UUID);
        } else if (email != null) {
            givenProfile = ProfileManager.getProfileFromEMail(email);
        } else if (discordID != null) {
            givenProfile = ProfileManager.getProfileFromDiscordID(discordID);
        }
        
        ServerResponseAccount response = new ServerResponseAccount(givenProfile);
        connection.sendTCP(response);
    }
}
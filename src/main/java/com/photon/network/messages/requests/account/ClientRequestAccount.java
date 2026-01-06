package com.photon.network.messages.requests.account;

/**
 * @author noz43
 */
public class ClientRequestAccount {
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
}
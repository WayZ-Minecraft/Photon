package com.photon.network.messages.response.account;

import com.photon.network.objects.ObjectPlayerAccount;

/**
 * @author noz43
 */
public class ServerResponseAccount {
    private final ObjectPlayerAccount givenProfile;
    
    public ServerResponseAccount(ObjectPlayerAccount givenProfile) {
        this.givenProfile = givenProfile;
    }
    
    public ObjectPlayerAccount getGivenProfile() { return givenProfile; }
}
package com.photon.network.messages.response.account;

import com.photon.network.objects.ObjectPlayerAccount;

/**
 * @author noz43
 */
public class ServerResponseValidAccount {
    private final boolean exist;
    private final boolean isValidPassword;
    private final boolean isEmailAlreadyUsed;
    private final boolean isUsernameAlreadyUsed;
    private final boolean isHWIDAlreadyUsed;
    private final ObjectPlayerAccount profile;
    
    public ServerResponseValidAccount(boolean exist, boolean isValidPassword, boolean isEmailAlreadyUsed, 
                                     boolean isUsernameAlreadyUsed, boolean isHWIDAlreadyUsed, 
                                     ObjectPlayerAccount profile) {
        this.exist = exist;
        this.isValidPassword = isValidPassword;
        this.isEmailAlreadyUsed = isEmailAlreadyUsed;
        this.isUsernameAlreadyUsed = isUsernameAlreadyUsed;
        this.isHWIDAlreadyUsed = isHWIDAlreadyUsed;
        this.profile = profile;
    }
    
    public boolean isExist() { return exist; }
    public boolean isValidPassword() { return isValidPassword; }
    public boolean isEmailAlreadyUsed() { return isEmailAlreadyUsed; }
    public boolean isUsernameAlreadyUsed() { return isUsernameAlreadyUsed; }
    public boolean isHWIDAlreadyUsed() { return isHWIDAlreadyUsed; }
    public ObjectPlayerAccount getProfile() { return profile; }
}
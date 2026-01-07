package com.photon.network.messages.response.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.objects.ObjectPlayerAccount;

public class ServerResponseValidAccount {
    public boolean exist;
    public boolean isValidPassword;
    public boolean isEmailAlreadyUsed;
    public boolean isUsernameAlreadyUsed;
    public boolean isHWIDAlreadyUsed;
    public ObjectPlayerAccount profile;
    
    public ServerResponseValidAccount(boolean exist, boolean isValidPassword, boolean isEmailAlreadyUsed, boolean isUsernameAlreadyUsed, boolean isHWIDAlreadyUsed, ObjectPlayerAccount profile) {
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
    
    public void handle(Connection connection) {
        PhotonEngine.clientAccountResponse = this;
        synchronized (PhotonEngine.clientAccountResponseWaiter) {
            PhotonEngine.clientAccountResponseWaiter.notify();
        }
    }
}
package com.photon.network.messages.response.account;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonClientData;
import com.photon.network.IPacket;
import com.photon.network.objects.ObjectPlayerAccount;

import org.jetbrains.annotations.Nullable;

/**
 * @author Niwer
 */
public class ServerResponseValidAccount implements IPacket {
    public boolean exist;
    public boolean isValidPassword;
    public boolean isEmailAlreadyUsed = false;
    public boolean isUsernameAlreadyUsed = false;
    public boolean isHWIDAlreadyUsed = false;
    public @Nullable ObjectPlayerAccount profile;
    
    public ServerResponseValidAccount() {
    }

    public ServerResponseValidAccount(boolean exist, boolean isValidPassword, @Nullable ObjectPlayerAccount profile) {
        this.exist = exist;
        this.isValidPassword = isValidPassword;
        this.profile = profile;
    }

    public ServerResponseValidAccount(boolean exist, boolean isValidPassword, boolean isEmailAlreadyUsed, boolean isUsernameAlreadyUsed, boolean isHWIDAlreadyUsed, @Nullable ObjectPlayerAccount profile) {
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
    public @Nullable ObjectPlayerAccount getProfile() { return profile; }
    
    @Override
    public void handle(Connection connection) {
        PhotonClientData.PLAYER_ACCOUNT_VERIF.set(this);
    }
}
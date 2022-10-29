package com.photon.network.messages.response.account;

import com.photon.network.objects.ObjectPlayerAccount;

public class ServerResponseValidAccount
{
	public boolean exist;
	public boolean isValidPassword;
	public boolean isEmailAlreadyUsed;
	public boolean isUsernameAlreadyUsed;
	public boolean isHWIDAlreadyUsed;
	public ObjectPlayerAccount profile;
}

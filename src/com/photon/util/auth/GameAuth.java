package com.photon.util.auth;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.photon.PhotonEngine;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.messages.requests.account.ClientRequestAccountCreation;
import com.photon.network.messages.requests.account.ClientRequestAccountVerification;
import com.photon.network.objects.ProfileManager;
import com.photon.util.ProtectorManager;

public class GameAuth
{
	public static boolean isAuthed = false;
	private static AuthError error;	
	private static Session session = new Session();
		
	public static boolean tryAuth(String email, String password) {
		if(!validEmailAddress(email)) { error = AuthError.NOT_VALID_EMAIL; return isAuthed = false; }
		if(password.length() < 8) { error = AuthError.PASSWORD_NOT_VALID; return isAuthed = false; }
		final ClientRequestAccountVerification verifyAccount = new ClientRequestAccountVerification();
		verifyAccount.email = email;
		verifyAccount.password = ProtectorManager.hash(password);
		NetworkConnectionClient.client.sendTCP(verifyAccount);
		while(PhotonEngine.clientAccountResponse == null) {}
		if(PhotonEngine.clientAccountResponse != null) {
			if(!PhotonEngine.clientAccountResponse.exist) { error = AuthError.ACCOUNT_NOT_FOUND; }
			else {
				if(!PhotonEngine.clientAccountResponse.isValidPassword) { error = AuthError.PASSWORD_NOT_VALID; }
				else { setSession(email, PhotonEngine.clientAccountResponse.profile.uuid); PhotonEngine.clientAccountResponse = null; return isAuthed = true; }
			}
		} else { error = AuthError.ACCOUNT_NOT_FOUND; }
		return isAuthed = false;
	}
	
	public static boolean tryCreateAccout(String email, String username, String password) {
		if(!validEmailAddress(email)) { error = AuthError.NOT_VALID_EMAIL; return isAuthed = false; }
		if(password.length() < 8) { error = AuthError.PASSWORD_NOT_VALID; return isAuthed = false; }
		final ClientRequestAccountCreation requestCreateAccount = new ClientRequestAccountCreation();
		requestCreateAccount.email = email;
		requestCreateAccount.username = username;
		requestCreateAccount.password = ProtectorManager.hash(password);
		NetworkConnectionClient.client.sendTCP(requestCreateAccount);
		while(PhotonEngine.clientAccountResponse == null) {}
		if(PhotonEngine.clientAccountResponse != null) {
			if(!PhotonEngine.clientAccountResponse.isUsernameAlreadyUsed) { error = AuthError.USERNAME_USED; }
			else { return isAuthed = true; }
		}
		return isAuthed = false;
	}
	
	private static void setSession(String email, String id) {
		session.setUsername(email);
		session.setToken(ProfileManager.getTokenFromEMail(email));
		session.setUuid(id);
	}
	
	public static boolean isLogged() { return isAuthed; }
	
	public static Session getSession() { return session; }
	
	public static AuthError getError() { return error; }
	
	public static String generateDigiCode() { return String.format("%06d", new Random().nextInt(999999)); }
	
	public static boolean validEmailAddress(String email) {
		Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        Matcher mat = pattern.matcher(email);
        if(mat.matches()) { return true; }
        return false;
	}
	
	public enum AuthError { NOT_VALID_EMAIL, UNDER8CHAR, ACCOUNT_NOT_FOUND, PASSWORD_NOT_VALID, USERNAME_USED, EMAIL_USED; }
}

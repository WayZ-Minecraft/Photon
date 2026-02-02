package com.photon.util.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

import com.photon.PhotonClientData;
import com.photon.network.ClientLinkManager;
import com.photon.network.messages.requests.account.ClientRequestAccountCreation;
import com.photon.network.messages.requests.account.ClientRequestAccountVerification;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.ProtectorManager;

public class PhotonUserAuthManager
{
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public static boolean isAuthed = false;
	private static AuthError error = AuthError.NO_RESPONSE;	
	private static PhotonAuthSession session = new PhotonAuthSession();
	
	/**
	 * Try to authenticate the user.
	 * @param email The email of the account
	 * @param password The password of the account
	 * @param hashPassword If the password should be hashed
	 * @return true if the user is authenticated
	 */
	public static boolean tryAuth(String email, String password, boolean hashPassword, Runnable callback) {
		setAuthed(false); // Reset the auth status
		setError(AuthError.NO_RESPONSE);// Reset the error status

		if(!validEmailAddress(email)) {
			setError(AuthError.NOT_VALID_EMAIL);
			return false; // Early return if the email is not valid
		}

		if(password.length() < 8) {
			setError(AuthError.UNDER_8_CHAR);
			return false; // Early return if the password is too short
		}
		
		/* Send a request to verify that the account exists */
		final ClientRequestAccountVerification REQUEST_ACCOUNT_CHECK = new ClientRequestAccountVerification(email, hashPassword ? ProtectorManager.hash(password) : password);
		ClientLinkManager.sendTCP(REQUEST_ACCOUNT_CHECK);
		
		/* Then when we have the response */
		PhotonClientData.PLAYER_ACCOUNT_VERIF.onAvailable(response -> {
			/* When we have a profile */
			PhotonClientData.PLAYER_ACCOUNT.onAvailable(account -> {
				if(!response.isExist()) setError(AuthError.ACCOUNT_NOT_FOUND);
				else {
					if(!response.isValidPassword()) setError(AuthError.PASSWORD_NOT_VALID);
					else setSession(account.username, account.uuid);
				}
				if(callback != null) callback.run();
			});
		});

		return isAuthed;
	}

	/**
	 * Try to authenticate the user. This will automatically hash the password
	 * @param email The email of the account
	 * @param password The password of the account
	 * @param saveAccount If the account should be saved
	 * @return true if the user is authenticated
	 */
	public static void tryCreateAccout(String email, String username, String password, Runnable callback) {
		setError(AuthError.NO_RESPONSE); // Reset the error status

		if(!validEmailAddress(email)) {
			setError(AuthError.NOT_VALID_EMAIL);
			return; // Early return if the email is not valid
		}
		if(password.length() < 8) {
			setError(AuthError.UNDER_8_CHAR);
			return; // Early return if the password is too short
		}
		
		/* Try create the account */
		final ClientRequestAccountCreation REQUEST_ACCOUNT_CREATE = new ClientRequestAccountCreation(username, email, ProtectorManager.hash(password));
		ClientLinkManager.sendTCP(REQUEST_ACCOUNT_CREATE);
				
		/* Then when we have the response */
		PhotonClientData.PLAYER_ACCOUNT_VERIF.onAvailable(response -> {
			if(response.isEmailAlreadyUsed()) setError(AuthError.EMAIL_USED);
			else if(response.isUsernameAlreadyUsed()) setError(AuthError.USERNAME_USED);
			else if(!response.isExist()) setError(AuthError.ACCOUNT_NOT_FOUND);
			else {
				setError(AuthError.SUCCESS);
				if(callback != null) callback.run();
			}
		});
	}

	/**
     * This try to connect the user to his account using the local saved file
     */
    public static void connectToAccountUsingLocalInfos(File path, Runnable callback) {
		try {
			final File ACCOUNT_FILE = new File(path, ProtectorManager.getHWID()+".accounts");
            if(ACCOUNT_FILE.exists()) {
				ConsoleManager.create("Try connecting using local infos...").withType(EnumLogType.CLIENT).end();
                final FileInputStream stream = new FileInputStream(ACCOUNT_FILE);
                final String[] content = new String(ProtectorManager.readCompressedFile(stream)).split("/");
                tryAuth(content[0], content[1], false, callback);
                stream.close();
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

	/**
	 * Save the account informations into a file
	 * @param path The path where the file will be saved
	 * @param email The email of the account
	 * @param password The password of the account
	 */
	public static void saveAccount(File path, String email, String password) {
        try {
			/* Create the file */
            final File ACCOUNT_FILE = new File(path, ProtectorManager.getHWID()+".accounts");
            if(!ACCOUNT_FILE.exists()) ACCOUNT_FILE.createNewFile();

			/* Write into the file */
            final String content = email+"/"+ProtectorManager.hash(password);
            ProtectorManager.writeCompressedFile(new FileOutputStream(ACCOUNT_FILE), content.getBytes());
        } catch (IOException e) { e.printStackTrace(); }
    }

	 /**
     * Check if the client has a account selected
     * @return true if the client has a account selected
     * @author Niwer
     */
	public static boolean hasAccountConnected() { return PhotonClientData.PLAYER_ACCOUNT.get() !=null; }

	public static ObjectPlayerAccount getProfile() { return PhotonClientData.PLAYER_ACCOUNT.get(); }

	public static String getTokenFromEMail(final String name) {
        final long TOKEN = Math.abs(SECURE_RANDOM.nextLong());
        return name + ":" + Long.toString(TOKEN, 16);
    }

	private synchronized static void setSession(String name, String uuid) {
		synchronized(session) {
			session.setUsername(name);
			session.setToken(getTokenFromEMail(name));
			session.setUuid(uuid);
			setAuthed(true);
			setError(AuthError.SUCCESS);
		}
	}
	
	public static boolean isLogged() { return isAuthed; }
	
	public static PhotonAuthSession getSession() { return session; }
	
	public static AuthError getError() { return error; }
	
	public static String generateDigiCode() { return String.format("%06d", new Random().nextInt(999999)); }
	
	public static boolean validEmailAddress(String email) {
		final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        return EMAIL_PATTERN.matcher(email).matches();
	}

	private synchronized static void setError(AuthError error) { PhotonUserAuthManager.error = error; }

	private synchronized static void setAuthed(boolean authed) { PhotonUserAuthManager.isAuthed = authed; }
	
	public enum AuthError {
		SUCCESS,
		NO_RESPONSE, 
		NOT_VALID_EMAIL, 
		PASSWORD_NOT_VALID, 
		UNDER_8_CHAR,
		ACCOUNT_NOT_FOUND, 
		USERNAME_USED, 
		EMAIL_USED,
		HWID_USED;
	}
}
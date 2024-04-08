package com.photon.util.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

import com.photon.PhotonEngine;
import com.photon.network.NetworkConnectionClient;
import com.photon.network.messages.requests.account.ClientRequestAccountCreation;
import com.photon.network.messages.requests.account.ClientRequestAccountVerification;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ProfileManager;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.ProtectorManager;

public class GameAuth
{
	public static boolean isAuthed = false;
	private static AuthError error = AuthError.NO_RESPONSE;	
	private static Session session = new Session();
	
	/**
	 * Try to authenticate the user.
	 * @param email The email of the account
	 * @param password The password of the account
	 * @param hashPassword If the password should be hashed
	 * @return true if the user is authenticated
	 */
	public static boolean tryAuth(String email, String password, boolean hashPassword, Runnable callback) {
		setAuthed(false); // Reset the auth status
		setError(AuthError.NO_RESPONSE); // Reset the error status

		Thread connectionThread = new Thread(() -> {
			if(!validEmailAddress(email)) setError(AuthError.NOT_VALID_EMAIL);
			if(password.length() < 8) setError(AuthError.UNDER_8_CHAR);

			synchronized(PhotonEngine.clientAccountResponseWaiter) {
				final ClientRequestAccountVerification REQUEST_ACCOUNT_CHECK = new ClientRequestAccountVerification();
				REQUEST_ACCOUNT_CHECK.email = email;
				REQUEST_ACCOUNT_CHECK.password = hashPassword ? ProtectorManager.hash(password) : password;
				NetworkConnectionClient.sendTCP(REQUEST_ACCOUNT_CHECK);

				try {
					PhotonEngine.clientAccountResponseWaiter.wait(); // Wait for the response
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(!PhotonEngine.clientAccountResponse.exist) setError(AuthError.ACCOUNT_NOT_FOUND);
				else {
					if(!PhotonEngine.clientAccountResponse.isValidPassword) setError(AuthError.PASSWORD_NOT_VALID);
					else setSession(getProfile().username, getProfile().uuid);
				}

				if(callback != null) callback.run();
			}
		});
		connectionThread.setName("Connection Thread");
		connectionThread.setDaemon(true);
		connectionThread.start();

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

		Thread connectionThread = new Thread(() -> {
			if(!validEmailAddress(email)) setError(AuthError.NOT_VALID_EMAIL);
			if(password.length() < 8) setError(AuthError.UNDER_8_CHAR);
			
			synchronized(PhotonEngine.clientAccountResponseWaiter) {
				final ClientRequestAccountCreation REQUEST_ACCOUNT_CREATE = new ClientRequestAccountCreation();
				REQUEST_ACCOUNT_CREATE.email = email;
				REQUEST_ACCOUNT_CREATE.username = username;
				REQUEST_ACCOUNT_CREATE.password = ProtectorManager.hash(password);
				NetworkConnectionClient.sendTCP(REQUEST_ACCOUNT_CREATE);
				
				try {
					PhotonEngine.clientAccountResponseWaiter.wait(); // Wait for the response
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(PhotonEngine.clientAccountResponse.isEmailAlreadyUsed) setError(AuthError.EMAIL_USED);
				else if(PhotonEngine.clientAccountResponse.isUsernameAlreadyUsed) setError(AuthError.USERNAME_USED);
				else if(!PhotonEngine.clientAccountResponse.exist) setError(AuthError.ACCOUNT_NOT_FOUND);
				else setError(AuthError.SUCCESS);
				if(callback != null) callback.run();
			}
		});
		connectionThread.setName("Connection Thread");
		connectionThread.setDaemon(true);
		connectionThread.start();
	}
	
	/**
     * This try to connect the user to his account using the local saved file
     */
    public static void connectToAccountUsingLocalInfos(File path, Runnable callback) {
		try {
			final File file = new File(path, ProtectorManager.getHWID()+".accounts");
            if(file.exists()) {
				ConsoleManager.create("Try connecting using local infos...").withType(EnumLogType.CLIENT).end();
                final FileInputStream stream = new FileInputStream(file);
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
            File file = new File(path, ProtectorManager.getHWID()+".accounts");
            if(!file.exists()) file.createNewFile();

            /* Write into the file */
            final String content = email+"/"+ProtectorManager.hash(password);
            ProtectorManager.writeCompressedFile(new FileOutputStream(file), content.getBytes());
        } catch (IOException e) { e.printStackTrace(); }
    }

	/**
     * Check if the client has a account selected
     * @return true if the client has a account selected
     * @author Niwer
     */
	public static boolean hasAccountConnected() { return PhotonEngine.clientAccountResponse !=null && PhotonEngine.clientAccountResponse.profile !=null; }

	public static ObjectPlayerAccount getProfile() { return PhotonEngine.clientAccountResponse.profile; }

	private synchronized static void setSession(String name, String uuid) {
		synchronized(session) {
			session.setUsername(name);
			session.setToken(ProfileManager.getTokenFromEMail(name));
			session.setUuid(uuid);
			setAuthed(true);
			setError(AuthError.SUCCESS);
		}
	}
	
	public static boolean isLogged() { return isAuthed; }
	
	public static Session getSession() { return session; }
	
	public static AuthError getError() { return error; }
	
	public static String generateDigiCode() { return String.format("%06d", new Random().nextInt(999999)); }
	
	public static boolean validEmailAddress(String email) {
		Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        return pattern.matcher(email).matches();
	}

	private synchronized static void setError(AuthError error) { GameAuth.error = error; }

	private synchronized static void setAuthed(boolean authed) { GameAuth.isAuthed = authed; }
	
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

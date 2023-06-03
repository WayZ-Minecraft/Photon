package com.photon.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.photon.network.NetworkDirectories;

public class ProtectorManager {

	private static byte[] getInfo() { return Base64.getEncoder().encode((NetworkDirectories.config.webUser + ":" + NetworkDirectories.config.webPassword).getBytes()); }
	
	private static String getAuth() { return "Basic " + new String(getInfo()); }
	
	public static void addProperties(URLConnection connection, String... exceptions) {
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		for(String ex : exceptions) {
			if(connection.getURL().toString().contains(ex)) return;
		}
		Authenticator.setDefault(new Authenticator() {
		    @Override protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(NetworkDirectories.config.webUser, NetworkDirectories.config.webPassword.toCharArray()); }
		});
		if(new String(getInfo()).equalsIgnoreCase(":")) connection.setRequestProperty("Authorization", ProtectorManager.getAuth());
	}
	
	public static String getHWID() {
		final String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
		return hash(toEncrypt);
    }
	
	public static String hash(String toHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(toHash.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) { hashtext = "0" + hashtext; }
            return hashtext;
        } catch (NoSuchAlgorithmException e) { return ""; } 
	}
	
	public static void writeCompressedFile(OutputStream stream, byte[] buffer) throws IOException {
		final DataOutputStream output = new DataOutputStream(new GZIPOutputStream(stream));
		output.writeInt(buffer.length);
		output.write(buffer);
		output.flush();
		output.close();
	}
	
	public static byte[] readCompressedFile(InputStream stream) throws IOException {
		final DataInputStream reader = new DataInputStream(new GZIPInputStream(stream));
		final byte[] buffer = new byte[reader.readInt()];
		reader.readFully(buffer);
		reader.close();
		return buffer;
	}
}

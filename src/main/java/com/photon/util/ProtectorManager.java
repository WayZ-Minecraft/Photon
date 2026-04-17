package com.photon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ProtectorManager {
	
	public static final int TIME_OUT = 15000;
	
	/**
	 * Adds properties to the URLConnection
	 * @param connection URLConnection
	 * @param exceptions URLs to exclude
	 * @return URLConnection
	 */
	public static URLConnection addProperties(URLConnection connection, String... exceptions) {
		connection.setConnectTimeout(TIME_OUT);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		return connection;
	}
	
	/**
	 * Gets the HWID of the computer
	 * @return HWID
	 */
	public static String getHWID() {
		final String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
		return hash(toEncrypt);
    }
	
	/**
	 * Hashes the ZIP entry
	 * @param entry ZipEntry
	 * @param zipFile ZipFile
	 * @param algorithm Algorithm
	 * @return Hash
	 */
	public static String hash(ZipEntry entry, ZipFile zipFile, String algorithm) {
		try (InputStream is = zipFile.getInputStream(entry); DigestInputStream dis = new DigestInputStream(is, MessageDigest.getInstance(algorithm))) {

			byte[] buffer = new byte[4096];
			while (dis.read(buffer) != -1) {
				// Reading and hashing simultaneously
			}
			return String.format("%032x", new BigInteger(1, dis.getMessageDigest().digest()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Hashes the input stream
	 * @param toHash InputStream
	 * @param algorithm Algorithm
	 * @return Hash
	 */
	public static String hash(InputStream toHash, String algorithm) {
		if (toHash == null) return "unknown";
		try (DigestInputStream stream = new DigestInputStream(toHash, MessageDigest.getInstance(algorithm))) {
			byte[] buffer = new byte[65536];
			while (stream.read(buffer) > 0) {
				// Reading and hashing simultaneously
			}
			return String.format("%032x", new BigInteger(1, stream.getMessageDigest().digest()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

	/**
	 * Hashes the file
	 * @param toHash Path
	 * @param algorithm Algorithm
	 * @return Hash
	 */
	public static String hash(Path toHash, String algorithm) { return toHash == null ? "unknown" : hash(toHash.toFile(), algorithm); }

	/**
	 * Hashes the file
	 * @param toHash File
	 * @param algorithm Algorithm
	 * @return Hash
	 */
	public static String hash(File toHash, String algorithm) {
		if (toHash == null) return "unknown";
		try (DigestInputStream stream = new DigestInputStream(new FileInputStream(toHash), MessageDigest.getInstance(algorithm))) {
			byte[] buffer = new byte[65536];
			while (stream.read(buffer) > 0) {
				// Reading and hashing simultaneously
			}
			return String.format("%032x", new BigInteger(1, stream.getMessageDigest().digest()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Hashes the string with SHA-1
	 * @param toHash String
	 * @return Hash
	 */
	public static String hash(String toHash) { return hash(toHash, "SHA-1"); }

	/**
	 * Hashes the string with the specified algorithm
	 * @param toHash String
	 * @param algorithm Algorithm
	 * @return Hash
	 */
	public static String hash(String toHash, String algorithm) {
        try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] messageDigest = md.digest(toHash.getBytes());
			StringBuilder hashtext = new StringBuilder(new BigInteger(1, messageDigest).toString(16));
			while (hashtext.length() < 32) hashtext.insert(0, '0');
			return hashtext.toString();
        } catch (NoSuchAlgorithmException e) { return ""; } 
	}
}
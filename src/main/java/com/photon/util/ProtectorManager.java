package com.photon.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.photon.network.NetworkDirectories;

public class ProtectorManager {

	public static final int FILE_FORMAT_VERSION_V1 = 0; // GZIP compression
	// public static final int FILE_FORMAT_VERSION_V2 = 1; // Zstd compression
	private static int currentFormatVersion = FILE_FORMAT_VERSION_V1;
	
	public static final int TIME_OUT = 15000;

	private static byte[] getInfo() { return Base64.getEncoder().encode((NetworkDirectories.getConfig().webUser + ":" + NetworkDirectories.getConfig().webPassword).getBytes()); }
	
	private static String getAuth() { return "Basic " + new String(getInfo()); }
	
	/**
	 * Adds properties to the URLConnection
	 * @param connection URLConnection
	 * @param exceptions URLs to exclude
	 * @return URLConnection
	 */
	public static URLConnection addProperties(URLConnection connection, String... exceptions) {
		connection.setConnectTimeout(TIME_OUT);

		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		for(String ex : exceptions) {
			if(connection.getURL().toString().contains(ex)) return connection;
		}
		Authenticator.setDefault(new Authenticator() {
		    @Override protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(NetworkDirectories.getConfig().webUser, NetworkDirectories.getConfig().webPassword.toCharArray()); }
		});
		if(new String(getInfo()).equalsIgnoreCase(":")) connection.setRequestProperty("Authorization", ProtectorManager.getAuth());
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

	/**
	 * Compress a string and return it as a byte array
	 * @param str String to compress
	 * @return byte[] of the compressed data
	 * @throws IOException
	 */
	public static byte[] compressFromString(String str) throws IOException { return compress(str.getBytes()); }

	/**
	 * Compress data and return it as a byte array
	 * @param buffer Buffer to compress
	 * @return byte[] of the compressed data
	 * @throws IOException
	 */
	public static byte[] compress(byte[] buffer) throws IOException {
		var output = new ByteArrayOutputStream();
		writeCompressedFile(output, buffer);
		return output.toByteArray();
	}

	/**
	 * Decompress a string and return it
	 * @param buffer Buffer to decompress
	 * @return String of the decompressed data
	 * @throws IOException
	 */
	public static String decompressToString(byte[] buffer) throws IOException {
		return new String(decompress(buffer));
	}

	/**
	 * Decompress data and return it as a byte array
	 * @param buffer Buffer to decompress
	 * @return byte[] of the decompressed data
	 * @throws IOException
	 */
	public static byte[] decompress(byte[] buffer) throws IOException {
		return readCompressedFile(new ByteArrayInputStream(buffer));
	}

	/**
	 * Compress data and write it to the stream
	 * @param stream OutputStream to write to
	 * @param buffer Buffer to write
	 * @throws IOException
	 */
	public static void writeCompressedFile(OutputStream stream, byte[] buffer) throws IOException {
		final DataOutputStream DOS = new DataOutputStream(stream);
		DOS.writeByte(currentFormatVersion);
		DOS.flush();
		
		OutputStream compressedStream;
		if(currentFormatVersion == FILE_FORMAT_VERSION_V1) compressedStream = new GZIPOutputStream(DOS);
		// else if(currentFormatVersion == FILE_FORMAT_VERSION_V2) compressedStream = new ZstdCompressorOutputStream(DOS);
		else throw new IOException("Unsupported file format version: " + currentFormatVersion);

		final DataOutputStream COMPRESSED_DOS = new DataOutputStream(compressedStream);
		COMPRESSED_DOS.writeInt(buffer.length);
		COMPRESSED_DOS.write(buffer);

		/* Flush and close */
		COMPRESSED_DOS.flush();
		COMPRESSED_DOS.close();
	}
	
	/**
	 * Read compressed data from the stream
	 * @param stream InputStream to read from
	 * @return byte[] of the data
	 * @throws IOException
	 */
	public static byte[] readCompressedFile(InputStream stream) throws IOException {
		return decodeFile(stream).b;
	}

	/**
	 * Decode the file from the stream
	 * @param stream InputStream to read from
	 * @return Pair of the version and the data
	 * @throws IOException
	 */
	public static Pair<Integer, byte[]> decodeFile(InputStream stream) throws IOException {
		BufferedInputStream bufferedStream = new BufferedInputStream(stream);
		bufferedStream.mark(1);
		final DataInputStream DIS = new DataInputStream(bufferedStream);
		final byte VERSION = DIS.readByte();
		
		/* Choose the compression format */
		InputStream decompressedStream;
		if (VERSION == FILE_FORMAT_VERSION_V1) decompressedStream = new GZIPInputStream(DIS);
		else if (VERSION == 31 /* If it's an older version of nebulae */) {
			// Reset the stream and treat it as if it has no version byte
			bufferedStream.reset();
			decompressedStream = new GZIPInputStream(DIS);
		}
		// else if (VERSION == FILE_FORMAT_VERSION_V2) decompressedStream = new ZstdCompressorInputStream(DIS);
		else throw new IOException("Unsupported file format version: " + VERSION);

		/* Read the compressed data */
		final DataInputStream READER = new DataInputStream(decompressedStream);
		final byte[] BUFFER = new byte[READER.readInt()];
		READER.readFully(BUFFER);

		/* Flush and close */
		READER.close();
		return Pair.of((int) VERSION, BUFFER);
	}
	
	/**
	 * Get the format version of the stream
	 * @param stream InputStream to read from
	 * @return The format version of the file in the stream
	 * @throws IOException
	 */
	public static int getFormatVersion(InputStream stream) throws IOException { return decodeFile(stream).a; }

	public static final class Pair<A, B> {
		public final A a;
		public final B b;

		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}

		public static <A, B> Pair<A, B> of(A a, B b) { return new Pair<>(a, b); }

		public A getA() { return a; }

		public B getB() { return b; }

		public boolean equals(Object o) { return this == o || (o instanceof Pair<?, ?> pair && a.equals(pair.a) && b.equals(pair.b)); }

		public int hashCode() { return 31 * a.hashCode() + b.hashCode(); }

		public String toString() { return "Pair{a=" + a + ", b=" + b + '}'; }
	}
}

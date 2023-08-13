package com.photon.informations;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.photon.network.NetworkDirectories;
import com.photon.util.ProtectorManager;

public class PhotonUpdaterManager {

    public static String url = NetworkDirectories.config.webUrl;
    private static ExecutorService downloader = Executors.newFixedThreadPool(5);

    /**
     * Get the latest SHA1 of the chosen update type
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @return The sha1 of the update if there is one, UNKNOWN otherwise
     */
    public static String getSHA1(UpdateFileType type) {
        /* If we can't reach the site, disable download */
        try {
			final URL url = new URL(NetworkDirectories.config.webUrl+"services_updates/the-sha.php?type="+type.name().toLowerCase());
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			ProtectorManager.addProperties(conn);
			conn.setRequestMethod("GET");

			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String sha1 = reader.readLine();
			reader.close();

			return sha1.contains("-") ? "UNKNOWN" : sha1;
		} catch (IOException e) { e.printStackTrace(); }
		return "UNKNOWN";
    }
    
    /**
     * Check if there is an update for the chosen type
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @param file The file to compare with
     * @return True if there is an update, false otherwise
     */
    public static boolean hasUpdate(UpdateFileType type, final File file) {
        final String sha1 = getSHA1(type);
        if(!file.exists()) return true;
        if(!sha1.equalsIgnoreCase("UNKNOWN") && !getDigest(file, "SHA", 40).equals(sha1)) return true;
        return false;
    }

    /**
     * Check if an update is available then download it
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @param file The file to compare with
     * @return True if the file has been downloaded, false otherwise
     */
    public static boolean update(UpdateFileType type, File file, RunnableTask<Integer, Integer> callback) {
        boolean hasFinished = false;
        if(hasUpdate(type, file)) hasFinished = download(type, file, callback);
        else hasFinished = true;

        if(hasFinished) downloader = Executors.newFixedThreadPool(5);
        return hasFinished;
    }

    /**
     * Get the latest URL of the chosen update type
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @return The url of the update if there is one, UNKNOWN otherwise
     */
    public static String getURL(UpdateFileType type) { return url+"services_updates/"+type.name().toLowerCase()+".jar"; }

    private static boolean download(UpdateFileType type, File file, RunnableTask<Integer, Integer> callback) {
        try {
            downloader.submit(new UpdateDownloader(file, getURL(type), callback));
            downloader.shutdown();
            downloader.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    private static class UpdateDownloader extends Thread {
        private final File file;
        private final String url;
        private final RunnableTask<Integer, Integer> callback;

        public UpdateDownloader(final File file, final String url, RunnableTask<Integer, Integer> callback) {
            this.file = file;
            this.url = url;
            this.callback = callback;
        }

        @Override
        public void run() {
            System.out.println("Acquiring file '" + file.getName() + "'");
            try {
                BufferedInputStream bufferedInputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    URL downloadUrl = new URL(url.replace(" ", "%20"));
                    URLConnection urlConnection = downloadUrl.openConnection();
                    ProtectorManager.addProperties(urlConnection);
                    bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
                    fileOutputStream = new FileOutputStream(file);
                    
                    final int size = 1024;
                    byte[] data = new byte[size];
                    int read;
                    int total = 0;
                    while ((read = bufferedInputStream.read(data, 0, size)) != -1) {
                        if(callback != null) callback.run(total+=read, urlConnection.getContentLength());
                        fileOutputStream.write(data, 0, read);
                    }
                } finally {
                    if (bufferedInputStream != null) bufferedInputStream.close();
                    if (fileOutputStream != null) fileOutputStream.close();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Get the digest of a file
     * @param file The file to get the digest from
     * @param algorithm The algorithm to use (e.g SHA, MD5)
     * @param hashLength The length of the hash
     * @return The digest of the file
     */
    public static String getDigest(File file, String algorithm, int hashLength) {
		DigestInputStream stream = null;
		try {
			stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(algorithm));
			byte[] ignored = new byte[65536];
			int read;
			do {
				read = stream.read(ignored);
			} while (read > 0);
			return String.format("%1$0" + hashLength + "x",
					new Object[] { new BigInteger(1, stream.getMessageDigest().digest()) });
		} catch (Exception localException) {
		} finally {
			try { stream.close(); } catch (Exception var2) { var2.printStackTrace(); }
		}
		return null;
	}

    public static enum UpdateFileType { MOD, LAUNCHER, API, NETWORK; }

    @FunctionalInterface
    public interface RunnableTask<T, X> {
        public abstract void run(T t, X x);
    }
}

package com.photon.util.updater;

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
import com.photon.util.ConsoleManager;
import com.photon.util.ProtectorManager;

public class UpdaterManager {

    public static final int DEFAULT_DOWNLOADER_THREADS_COUNT = 5;
    private static ExecutorService downloader = null;

    /**
     * Get the latest SHA1 of the chosen update type
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @return The sha1 of the update if there is one, UNKNOWN otherwise
     */
    public static String getSHA1(UpdateFileType type, UpdateChannel channel) {
        /* If we can't reach the site, disable download */
        try {
			final URL url = new URL(NetworkDirectories.getConfig().webUrl+"services_updates/the-sha.php?type="+type.name().toLowerCase()+"&channel="+channel.name().toLowerCase());
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
    public static boolean hasUpdate(UpdateFileType type, UpdateChannel channel, final File file) {
        final String sha1 = getSHA1(type, channel);
        if(!file.exists()) return true;
        if(!sha1.equalsIgnoreCase("UNKNOWN") && !getDigest(file, "SHA", 40).equals(sha1)) return true;
        return false;
    }

    /**
     * Check if an update is available for specicifc type then download it.
     * E.G : You can update the MOD for the STABLE channel
     * @note This method don't care about the channel, it will always download the latest STABLE version !
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @param file The file to compare with
     * @param callback The callback to call when the download is finished
     * @return True if the file has been downloaded, false otherwise
     */
    public static boolean update(UpdateFileType type, File file, RunnableTask<Integer, Integer> callback) {
        return update(type, UpdateChannel.STABLE, file, callback);
    }

    /**
     * Check if an update is available for specicifc type and channel then download it.
     * E.G : You can update the MOD for the STABLE or DEV channel
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @param channel The channel of the update (e.g STABLE, DEV)
     * @param file The file to compare with
     * @return True if the file has been downloaded, false otherwise
     */
    public static boolean update(UpdateFileType type, UpdateChannel channel, File file, RunnableTask<Integer, Integer> callback) {
        /* If the downloader is not defined set it to default value */
        if(downloader == null) downloader = Executors.newFixedThreadPool(DEFAULT_DOWNLOADER_THREADS_COUNT);

        /* Download */
        boolean hasFinished = false;
        if(hasUpdate(type, channel, file)) hasFinished = download(type, channel, file, callback);
        else hasFinished = true;

        /* Reset downloader */
        if(hasFinished) downloader = null;
        return hasFinished;
    }

    /**
     * Get the latest URL of the chosen update type and channel in the format 
     * https://.../services_updates/{type}-{channel}.jar (e.g https://.../services_updates/mod-dev.jar)
     * EXCEPT if the channel is STABLE, then the format will be 
     * https://.../services_updates/{type}.jar (e.g https://.../services_updates/mod.jar)
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @return The url of the update if there is one, UNKNOWN otherwise
     */
    public static String getURL(UpdateFileType type, UpdateChannel channel) {
        String result = NetworkDirectories.getConfig().webUrl + "services_updates/" +
            type.name().toLowerCase() +
            (channel != UpdateChannel.STABLE ? "-" + channel.name().toLowerCase() : "") +
            ".jar";

        try {
            URL resultUrl = new URL(result);
            HttpURLConnection resultConn = (HttpURLConnection) resultUrl.openConnection();
            resultConn.setRequestMethod("HEAD");
            if (resultConn.getResponseCode() == HttpURLConnection.HTTP_OK) return result;
            else ConsoleManager.create("Unable to fetch : " + result + ", fallback on the stable channel").error().end();
        } catch (IOException e) {
            ConsoleManager.create("Unable to fetch : " + result + ", fallback on the stable channel").error().end();
        }
        return getURL(type, UpdateChannel.STABLE);
    }

    /**
     * Allow to choose the ammount of threads used to download the update of a file
     * @param downloaderThreadsCount The ammount of threads to use
     */
    public static void setDownloaderThreadsCount(int downloaderThreadsCount) {
        if(downloader == null) downloader = Executors.newFixedThreadPool(downloaderThreadsCount);
    }

    private static boolean download(UpdateFileType type, UpdateChannel channel, File file, RunnableTask<Integer, Integer> callback) {
        try {
            downloader.submit(new UpdateDownloader(file, getURL(type, channel), callback));
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
                        if(callback != null) callback.run(total += read, urlConnection.getContentLength());
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

    @FunctionalInterface
    public interface RunnableTask<T, X> {
        public abstract void run(T t, X x);
    }
}
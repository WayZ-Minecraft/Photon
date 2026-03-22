package com.photon.util.updater;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.photon.PhotonClientData;
import com.photon.network.ClientLinkManager;
import com.photon.network.messages.requests.ClientRequestUpdate;

public class UpdaterManager {

    public static final int DEFAULT_DOWNLOADER_THREADS_COUNT = 5;
    private static ExecutorService downloader = null;

    /**
     * Get the latest SHA1 of the chosen update type
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @return The sha1 of the update if there is one, UNKNOWN otherwise
     */
    public static String getSHA1(UpdateFileType type, UpdateChannel channel) {
        PhotonClientData.UPDATE_SHA.reset();
        ClientLinkManager.sendTCP(new ClientRequestUpdate(channel, type)); // Ask the server for the update data (SHA and File)

        final CountDownLatch LATCH = new CountDownLatch(1);
        PhotonClientData.UPDATE_SHA.onAvailable(v -> LATCH.countDown());
        
        try {
            LATCH.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		return PhotonClientData.UPDATE_SHA.get();
    }
    
    /**
     * Check if there is an update for the chosen type
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @param file The file to compare with
     * @return True if there is an update, false otherwise
     */
    public static boolean hasUpdate(UpdateFileType type, UpdateChannel channel, final File file) {
        if(!file.exists()) return true; // If the file doesn't exist, we need to download it

        final String SHA_1 = getSHA1(type, channel);
        if(!SHA_1.equalsIgnoreCase("UNKNOWN") && !getUpdateDigest(file, "SHA", 40).equals(SHA_1)) return true;
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
        if(downloader == null) downloader = Executors.newFixedThreadPool(DEFAULT_DOWNLOADER_THREADS_COUNT);

        boolean hasFinished = false;
        if(hasUpdate(type, channel, file)) hasFinished = download(type, channel, file, callback);
        else hasFinished = true;

        if(hasFinished) downloader = null;
        return hasFinished;
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
            downloader.submit(new UpdateDownloader(file, callback));
            downloader.shutdown();
            downloader.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    private static class UpdateDownloader extends Thread {
        private final File file;
        private final RunnableTask<Integer, Integer> callback;

        public UpdateDownloader(final File file, RunnableTask<Integer, Integer> callback) {
            this.file = file;
            this.callback = callback;
        }

        @Override
        public void run() {
            System.out.println("Acquiring file '" + file.getName() + "'");
            try(var bis = new BufferedInputStream(new ByteArrayInputStream(PhotonClientData.UPDATE_DATA.get())); var fos = new FileOutputStream(file)) {
                
                final int UPDATE_SIZE = PhotonClientData.UPDATE_DATA.get().length;
                final int SIZE = 1024;
                final byte[] DATA = new byte[SIZE];

                int read;
                int total = 0;
                while ((read = bis.read(DATA, 0, SIZE)) != -1) {
                    if(callback != null) callback.run(total += read, UPDATE_SIZE);
                    fos.write(DATA, 0, read);
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
    public static String getUpdateDigest(File file, String algorithm, int hashLength) {
		try(var stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(algorithm))) {
			final byte[] IGNORED = new byte[65536];

			int read;
			do {
				read = stream.read(IGNORED);
			} while (read > 0);

			return String.format("%1$0" + hashLength + "x", new Object[] { new BigInteger(1, stream.getMessageDigest().digest()) });
		} catch (Exception ex) {}
		return null;
	}

    @FunctionalInterface
    public interface RunnableTask<T, X> {
        public abstract void run(T t, X x);
    }
}
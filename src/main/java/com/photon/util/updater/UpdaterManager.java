package com.photon.util.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Duration;

import com.photon.Directories;

public class UpdaterManager {

    public static final int DEFAULT_DOWNLOADER_THREADS_COUNT = 5;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    /**
     * Get the latest SHA1 of the chosen update type
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @return The sha1 of the update if there is one, UNKNOWN otherwise
     */
    public static String getSHA1(UpdateFileType type, UpdateChannel channel) {
        try {
            final UpdateMetadata metadata = fetchMetadata(type, channel);
            return metadata != null ? metadata.sha1() : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Check if there is an update for the chosen type
     * @param type The type of the update (e.g MOD, LAUNCHER, API, NETWORK)
     * @param file The file to compare with
     * @return True if there is an update, false otherwise
     */
    public static boolean hasUpdate(UpdateFileType type, UpdateChannel channel, final File file) {
        if(!file.exists()) return true; // If the file doesn't exist, we need to download it

        final String sha1 = getSHA1(type, channel);
        final String localSha = getUpdateDigest(file, "SHA", 40);
        if (!"UNKNOWN".equalsIgnoreCase(sha1) && localSha != null && !localSha.equalsIgnoreCase(sha1)) return true;
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
        if(hasUpdate(type, channel, file)) return download(type, channel, file, callback);
        return true;
    }

    /**
     * Allow to choose the ammount of threads used to download the update of a file
     * @param downloaderThreadsCount The ammount of threads to use
     */
    public static void setDownloaderThreadsCount(int downloaderThreadsCount) {
        // HTTP downloads are now synchronous. Kept for API compatibility.
    }

    private static boolean download(UpdateFileType type, UpdateChannel channel, File file, RunnableTask<Integer, Integer> callback) {
        try {
            final UpdateDownload download = fetchDownload(type, channel);
            if (download == null || download.data() == null) return false;

            if (file.getParentFile() != null && !file.getParentFile().exists()) file.getParentFile().mkdirs();
            Files.write(file.toPath(), download.data());
            if (callback != null) callback.run(download.data().length, download.data().length);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        return false;
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

    private static UpdateMetadata fetchMetadata(UpdateFileType type, UpdateChannel channel) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder(buildUpdateUri(type, channel, true))
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build();

        final HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) return null;

        return Directories.GSON.fromJson(new String(response.body(), StandardCharsets.UTF_8), UpdateMetadata.class);
    }

    private static UpdateDownload fetchDownload(UpdateFileType type, UpdateChannel channel) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder(buildUpdateUri(type, channel, false))
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build();

        final HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) return null;

        final String sha1 = response.headers().firstValue("x-photon-sha1").orElse("UNKNOWN");
        return new UpdateDownload(sha1, response.body());
    }

    private static URI buildUpdateUri(UpdateFileType type, UpdateChannel channel, boolean metadata) {
        // final String host = PhotonEngine.network_Ip == null || PhotonEngine.network_Ip.isBlank() ? PhotonEngine.LOCAL_IP : PhotonEngine.network_Ip;
        final String host = "update.photonmc.com"; //TODO
        return URI.create("http://" + host + ":" + Directories.getConfig().webserver_port + "/api/update?type=" + type.name() + "&channel=" + channel.name() + "&metadata=" + metadata);
    }

    private record UpdateMetadata(String sha1, int size) {}

    private record UpdateDownload(String sha1, byte[] data) {}

    @FunctionalInterface
    public interface RunnableTask<T, X> {
        public abstract void run(T t, X x);
    }
}

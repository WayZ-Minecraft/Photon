package com.photon.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.photon.Directories;
import com.photon.PhotonClientData;
import com.photon.Directories.NetworkConfig;
import com.photon.objects.ObjectServer;
import com.photon.util.ProtectorManager;

public class NetworkWebClient {

    private NetworkWebClient() {}

    public static NetworkConfig fetchNetworkConfig(String host, int port) throws IOException {
        //TODO we are sending sensitive data to the client with NetworkConfig !
        final String body = readBody(buildUrl(host, port, "/api/network-config"));
        return Directories.GSON.fromJson(body, NetworkConfig.class);
    }

    public static List<ObjectServer> fetchServerList(String host, int port) throws IOException {
        final String body = readBody(buildUrl(host, port, "/api/server-list"));
        final ObjectServer[] serverArray = Directories.GSON.fromJson(body, ObjectServer[].class);
        if (serverArray == null || serverArray.length == 0) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(serverArray));
    }

    public static void refreshServerList(String host, int port) throws IOException {
        PhotonClientData.CLIENT_SERVER_LIST.clear();
        PhotonClientData.CLIENT_SERVER_LIST.addAll(fetchServerList(host, port));
    }

    private static String readBody(String url) throws IOException {
        final URLConnection connection = ProtectorManager.addProperties(new URL(url).openConnection());
        try (InputStream stream = connection.getInputStream()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String buildUrl(String host, int port, String path) {
        return "http://" + host + ":" + port + path;
    }
}
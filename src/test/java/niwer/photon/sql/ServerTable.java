package niwer.photon.sql;

import java.util.ArrayList;
import java.util.List;

import niwer.photon.objects.ObjectServer;

public final class ServerTable {

    private static ObjectServer lastSavedServer;
    private static List<ObjectServer> visibleServers = new ArrayList<>();

    private ServerTable() {}

    public static void reset() {
        lastSavedServer = null;
        visibleServers = new ArrayList<>();
    }

    public static void setVisibleServers(List<ObjectServer> servers) {
        visibleServers = new ArrayList<>(servers);
    }

    public static void saveOrUpdate(ObjectServer server) {
        lastSavedServer = server;
    }

    public static List<ObjectServer> getVisibleServers() {
        return visibleServers;
    }

    public static ObjectServer getServer(String ip, int port) {
        return null;
    }

    public static ObjectServer lastSavedServer() { return lastSavedServer; }
}
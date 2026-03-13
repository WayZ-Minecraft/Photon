package com.photon.network.messages.requests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.IPacket;
import com.photon.network.NetworkDirectories;
import com.photon.network.messages.response.ServerResponseUpdate;
import com.photon.util.ProtectorManager;
import com.photon.util.updater.UpdateChannel;
import com.photon.util.updater.UpdateFileType;

import niwer.lumen.Console;

/**
 * @author Niwer
 */
public class ClientRequestUpdate implements IPacket {
    private final UpdateChannel channel;
    private final UpdateFileType type;
    
    public ClientRequestUpdate() {
        this.channel = null;
        this.type = null;
    }

    public ClientRequestUpdate(UpdateChannel fileMessage, UpdateFileType fileName) {
        this.channel = fileMessage;
        this.type = fileName;
    }
    
    @Override
    public void handle(Connection connection) {
        try {
            final String updatePath = NetworkDirectories.getPathForUpdateChannel(type, channel);
            final File updateFile = new File(updatePath);
            if(!updateFile.exists()) {
                Console.log("Unable to find the requested version file. Skipping update.").error().container(PhotonEngine.LOGGER).send();
                return;
            }

            final String sha1 = ProtectorManager.hash(updateFile, "SHA-1");
            final byte[] data = readFileToByteArray(updateFile);
            final ServerResponseUpdate response = new ServerResponseUpdate(data, sha1);
            connection.sendTCP(response); // Send back to the client
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readFileToByteArray(final File file) throws IOException {
        final byte[] data = new byte[(int) file.length()];
        final FileInputStream fis = new FileInputStream(file);
        fis.read(data);
        fis.close();
        return data;
    }
}
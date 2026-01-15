package com.photon;

import java.io.IOException;

import com.photon.network.ClientLinkManager;
import com.photon.network.messages.requests.ClientRequestAnticheat;
import com.photon.network.messages.requests.ClientRequestCrashReport;
import com.photon.util.ConsoleManager;
import com.photon.util.updater.UpdateChannel;
import com.photon.util.updater.UpdateFileType;
import com.photon.util.updater.UpdaterManager;

public class NetworkClientSideTester {

    public static void main(String[] args) {
        try {
            /* Connecting to local network after trying to connect to a random IP */
            PhotonEngine.loadClient("111.111.111.111", true);

            /* Try to send some packets */
            {
                final var anticheat = new ClientRequestAnticheat(
                    "FileName", "Content", "Windows 10", "UserID"
                );
                ClientLinkManager.sendTCP(anticheat);

                final var crashreport = new ClientRequestCrashReport(
                    "Content", "FileName", "UserID"
                );
                ClientLinkManager.sendTCP(crashreport);

                final var news = new ClientRequestCrashReport(
                    "Content", "FileName", "UserID"
                );
                ClientLinkManager.sendTCP(news);

                UpdaterManager.getSHA1(UpdateFileType.API, UpdateChannel.STABLE);
            }
        } catch (IOException e) {
            ConsoleManager.debug("Error when connecting: " + e.getMessage());
        }
    }
}
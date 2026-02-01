package com.photon;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.photon.network.ClientLinkManager;
import com.photon.network.NetworkDirectories;
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

            /* Test logo functionality */
            {
                ConsoleManager.create("=== LOGO TEST ===").end();
                
                BufferedImage logo = NetworkDirectories.getGameLogo();
                
                if (logo == null) {
                    ConsoleManager.create("Logo is NULL - not received from server yet or failed to load").error().end();
                } else {
                    ConsoleManager.create("Logo loaded successfully!").end();
                    ConsoleManager.create("  - Width: " + logo.getWidth() + "px").end();
                    ConsoleManager.create("  - Height: " + logo.getHeight() + "px").end();
                    ConsoleManager.create("  - Type: " + logo.getType()).end();
                    
                    byte[] logoData = NetworkDirectories.getConfig().gameLogo;
                    if (logoData != null) {
                        ConsoleManager.create("  - Size: " + logoData.length + " bytes (" + (logoData.length / 1024) + " KB)").end();
                    }
                }
                
                ConsoleManager.create("=== END LOGO TEST ===").end();
            }
        } catch (IOException e) {
            ConsoleManager.debug("Error when connecting: " + e.getMessage());
        }
    }
}
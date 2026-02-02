package com.photon;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.photon.network.ClientLinkManager;
import com.photon.network.NetworkDirectories;
import com.photon.network.NetworkDirectories.NetworkConfig;
import com.photon.network.NetworkEngine;
import com.photon.network.messages.requests.ClientRequestAnticheat;
import com.photon.network.messages.requests.ClientRequestCrashReport;
import com.photon.network.messages.requests.news.ClientRequestNewsList;
import com.photon.util.ConsoleManager;
import com.photon.util.NetworkOnly;
import com.photon.util.auth.PhotonUserAuthManager;
import com.photon.util.updater.UpdateChannel;
import com.photon.util.updater.UpdateFileType;
import com.photon.util.updater.UpdaterManager;

@NetworkOnly // This class is only for network testing purposes
public class NetworkClientSideTester {

    public static void main(String[] args) {
        try {
            NetworkEngine.load(args); // Start the Network Engine
            ConsoleManager.create("\n\n").end();

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

                final var news = new ClientRequestNewsList();
                ClientLinkManager.sendTCP(news);

                /* Update Test */
                String data = UpdaterManager.getSHA1(UpdateFileType.API, UpdateChannel.STABLE); //TODO
            }

            {
                /* Official Accounts System */
                PhotonUserAuthManager.tryCreateAccout("test@mailhost.com", "Tester", "MySuperProtectedPassWord1*", () -> ConsoleManager.debug("Account Create on the server and we receive it !"));

                PhotonUserAuthManager.tryAuth("test@mailhost.com", "MySuperProtectedPassWord1", true, () -> ConsoleManager.debug("Your are successfully authenticated !"));
            }

            /* Test logo functionality */
            {                
                final BufferedImage LOGO = NetworkConfig.getGameLogo();
                if (LOGO == null) ConsoleManager.create("Logo is NULL - not received from server yet or failed to load").error().end();
                else {
                    ConsoleManager.create("Logo loaded successfully!").end();
                    ConsoleManager.create("  - Width: " + LOGO.getWidth() + "px").end();
                    ConsoleManager.create("  - Height: " + LOGO.getHeight() + "px").end();
                    ConsoleManager.create("  - Type: " + LOGO.getType()).end();
                    
                    final byte[] LOGO_DATA = NetworkDirectories.getConfig().gameLogo;
                    if (LOGO_DATA != null) ConsoleManager.create("  - Size: " + LOGO_DATA.length + " bytes (" + (LOGO_DATA.length / 1024) + " KB)").end();
                }
            }
        } catch (IOException e) {
            ConsoleManager.debug("Error when connecting: " + e.getMessage());
        }
    }
}
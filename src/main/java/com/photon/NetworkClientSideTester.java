package com.photon;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

import com.photon.network.ClientLinkManager;
import com.photon.network.NetworkDirectories;
import com.photon.network.NetworkDirectories.NetworkConfig;
import com.photon.network.NetworkEngine;
import com.photon.network.messages.requests.ClientRequestAnticheat;
import com.photon.network.messages.requests.ClientRequestCrashReport;
import com.photon.network.messages.requests.ClientRequestHWID;
import com.photon.network.messages.requests.news.ClientRequestNewsList;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;
import com.photon.util.NetworkOnly;
import com.photon.util.auth.PhotonUserAuthManager;
import com.photon.util.os.OperatingSystem;

@NetworkOnly // This class is only for network testing purposes
public class NetworkClientSideTester {

    public static void main(String[] args) {
        try {
            NetworkEngine.load(args); // Start the Network Engine
            ConsoleManager.create("\n\n").end();

            /* Connecting to local network after trying to connect to a random IP */
            PhotonEngine.loadClient(PhotonEngine.LOCAL_IP);

            final String GENERATED_USERNAME = "TestUser_" + System.currentTimeMillis() % 1000;
            final String GENERATED_UUID = UUID.randomUUID().toString();
            final OperatingSystem GENERATED_OS = OperatingSystem.values()[(int)(System.currentTimeMillis() % OperatingSystem.values().length)];

            /* Try to send some packets */
            {
                final var anticheat = new ClientRequestAnticheat("FileName", "Content", GENERATED_OS, GENERATED_UUID);
                ClientLinkManager.sendTCP(anticheat);

                final var crashreport = new ClientRequestCrashReport("Content", "FileName", GENERATED_UUID);
                ClientLinkManager.sendTCP(crashreport);

                final var hwid = new ClientRequestHWID(GENERATED_USERNAME, GENERATED_UUID, "MONHWID", GENERATED_OS);
                ClientLinkManager.sendTCP(hwid);

                final var news = new ClientRequestNewsList();
                ClientLinkManager.sendTCP(news);

                /* Update Test */
                // String data = UpdaterManager.getSHA1(UpdateFileType.API, UpdateChannel.STABLE); //TODO
            }

            /* Accounts */
            {
                /* Official Accounts System */
                PhotonUserAuthManager.tryCreateAccout("test_bis@mailhost.com", "Tester_bis", "Passwooooorddddddddd*", () -> ConsoleManager.create("A. Account Create on the server and we receive it !").withType(EnumLogType.CLIENT).end());
                PhotonUserAuthManager.tryCreateAccout("test@mailhost.com", "Tester", "MySuperProtectedPassWord1*", () -> ConsoleManager.create("B. Account Create on the server and we receive it !").withType(EnumLogType.CLIENT).end());

                PhotonUserAuthManager.tryAuth("test@mailhost.com", "MySuperProtectedPassWord1", true, () -> ConsoleManager.create("Your are successfully authenticated !").withType(EnumLogType.CLIENT).end());
            }

            /* Test logo functionality */
            {                
                final BufferedImage LOGO = NetworkConfig.getGameLogo();
                if (LOGO == null) ConsoleManager.create("Logo is NULL - not received from server yet or failed to load").error().end();
                else
                    ConsoleManager.create(
                        String.format("Logo loaded successfully (Width: %dpx/Height: %dpx/Type: %d - Size : %d Kb)", LOGO.getWidth(), LOGO.getHeight(), LOGO.getType(), NetworkDirectories.getConfig().gameLogo.length / 1024)
                    ).end();
            }
        } catch (IOException e) {
            ConsoleManager.debug("Error when connecting: " + e.getMessage());
        }
    }
}
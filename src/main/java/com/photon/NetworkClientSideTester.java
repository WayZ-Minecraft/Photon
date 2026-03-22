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
import com.photon.sql.DiscordProfileTable;
import com.photon.util.NetworkOnly;
import com.photon.util.PhotonLogTypes;
import com.photon.util.auth.PhotonUserAuthManager;
import com.photon.util.os.OperatingSystem;
import com.photon.util.updater.UpdateChannel;
import com.photon.util.updater.UpdateFileType;
import com.photon.util.updater.UpdaterManager;

import niwer.lumen.Console;

@NetworkOnly // This class is only for network testing purposes
public class NetworkClientSideTester {

    public static void main(String[] args) {
        try {
            NetworkEngine.load(args); // Start the Network Engine
            Console.log("\n\n").container(PhotonEngine.LOGGER).send();

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
                String data = UpdaterManager.getSHA1(UpdateFileType.API, UpdateChannel.STABLE); //TODO
                Console.log("Received SHA1 for API update: " + data).type(PhotonLogTypes.TESTER).send();
            }

            /* Accounts */
            {
                /* Official Accounts System */
                PhotonUserAuthManager.tryCreateAccout("test_bis@mailhost.com", "Tester_bis", "Passwooooorddddddddd*", () -> Console.log("A. Account Create on the server and we receive it !").type(PhotonLogTypes.TESTER).send());
                PhotonUserAuthManager.tryCreateAccout("test@mailhost.com", "Tester", "MySuperProtectedPassWord1*", () -> Console.log("B. Account Create on the server and we receive it !").type(PhotonLogTypes.TESTER).send());

                PhotonUserAuthManager.tryAuth("test@mailhost.com", "MySuperProtectedPassWord1", true, () -> Console.log("Your are successfully authenticated !").type(PhotonLogTypes.TESTER).send());
            }

            /* Test logo functionality */
            {                
                final BufferedImage LOGO = NetworkConfig.getGameLogo();
                if (LOGO == null) Console.log("Logo is NULL - not received from server yet or failed to load").error().send();
                else
                    Console.log(
                        String.format("Logo loaded successfully (Width: %dpx/Height: %dpx/Type: %d - Size : %d Kb)", LOGO.getWidth(), LOGO.getHeight(), LOGO.getType(), NetworkDirectories.getConfig().gameLogo.length / 1024)
                    ).container(PhotonEngine.LOGGER).send();
            }

            /* Discord table test */
            DiscordProfileTable.createProfile(GENERATED_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
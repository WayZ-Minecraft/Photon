package niwer.photon;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Handler;

import org.junit.jupiter.api.Test;

class PhotonEngineTest {

    @Test
    void getCurrentIpReturnsCachedValueWithoutNetworkCall() throws Exception {
        final Field field = PhotonEngine.class.getDeclaredField("currentIP");
        field.setAccessible(true);
        final String originalValue = (String) field.get(null);

        try {
            field.set(null, "203.0.113.7");

            assertEquals("203.0.113.7", PhotonEngine.getCurrentIP());
        } finally {
            field.set(null, originalValue);
        }
    }

    @Test
    void isIpEqualsUsesCurrentIpCache() throws Exception {
        final Field field = PhotonEngine.class.getDeclaredField("currentIP");
        field.setAccessible(true);
        final String originalValue = (String) field.get(null);

        try {
            field.set(null, "10.0.0.1");

            assertTrue(PhotonEngine.isIPEquals("10.0.0.1"));
            assertTrue(PhotonEngine.isIPEquals("10.0.0.1".toUpperCase()));
            assertFalse(PhotonEngine.isIPEquals("10.0.0.2"));
        } finally {
            field.set(null, originalValue);
        }
    }

    @Test
    void getDateFormatsDatesUsingTheExpectedPattern() {
        final TimeZone originalTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            assertEquals("01-00-1970", PhotonEngine.getDate(false, new Date(0L)));
            assertEquals("01-00-1970_00-00-00", PhotonEngine.getDate(true, new Date(0L)));
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }

    @Test
    void isOnlineFailsForUnknownHosts() {
        assertThrows(java.net.UnknownHostException.class, () -> PhotonEngine.isOnline("definitely-not-a-real-host.invalid"));
    }

    @Test
    void isOnlineHandlesLoopbackWithoutThrowing() {
        assertDoesNotThrow(() -> PhotonEngine.isOnline("127.0.0.1"));
    }

    @Test
    void mainSkipsBotStartupWhenTokenIsMissing() throws Exception {
        final Path configFile = Files.createTempFile("photon-config-empty", ".json");
        writeConfig(configFile, "");

        final FileSnapshot snapshot = FileSnapshot.capture();
        try {
            resetLoggerHandlers();
            resetDatabaseRegistry();
            Directories.configFile = configFile.toFile();
            niwer.photon.discord.BotEngineTest.reset();
            assertDoesNotThrow(() -> PhotonEngine.main(new String[0]));

            assertFalse(niwer.photon.discord.BotEngineTest.wasLoadCalled());
        } finally {
            resetLoggerHandlers();
            resetDatabaseRegistry();
            snapshot.restore();
            Files.deleteIfExists(configFile);
        }
    }

    @Test
    void mainStartsBotBranchWhenTokenIsPresent() throws Exception {
        final Path configFile = Files.createTempFile("photon-config-token", ".json");
        writeConfig(configFile, "token-123");

        final FileSnapshot snapshot = FileSnapshot.capture();
        try {
            resetLoggerHandlers();
            resetDatabaseRegistry();
            Directories.configFile = configFile.toFile();
            niwer.photon.discord.BotEngineTest.reset();
            assertDoesNotThrow(() -> PhotonEngine.main(new String[] { "--restart" }));

            assertTrue(niwer.photon.discord.BotEngineTest.wasLoadCalled());
            assertTrue(niwer.photon.discord.BotEngineTest.lastRestartValue());
        } finally {
            resetLoggerHandlers();
            resetDatabaseRegistry();
            snapshot.restore();
            Files.deleteIfExists(configFile);
        }
    }

    private static void resetLoggerHandlers() {
        for (Handler handler : PhotonEngine.LOGGER.logger().getHandlers()) {
            PhotonEngine.LOGGER.logger().removeHandler(handler);
            handler.close();
        }
    }

    private static void resetDatabaseRegistry() {
        try {
            final Field registryField = PhotonEngine.DATA_BASE.getClass().getDeclaredField("REGISTERED_TABLES");
            registryField.setAccessible(true);
            @SuppressWarnings("unchecked")
            final java.util.Set<Object> registeredTables = (java.util.Set<Object>) registryField.get(PhotonEngine.DATA_BASE);
            registeredTables.clear();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to reset PhotonEngine database registry", e);
        }
    }

    private static void writeConfig(Path configFile, String discordToken) throws IOException {
        final Directories.NetworkConfig config = new Directories.NetworkConfig();
        config.discord_bot_token = discordToken;
        Files.writeString(configFile, Directories.GSON.toJson(config));
    }

    private record FileSnapshot(Path configFile, String currentIp) {
        static FileSnapshot capture() throws Exception {
            final Field configFileField = Directories.class.getDeclaredField("configFile");
            configFileField.setAccessible(true);

            final Field currentIpField = PhotonEngine.class.getDeclaredField("currentIP");
            currentIpField.setAccessible(true);

            final Path configPath = ((java.io.File) configFileField.get(null)).toPath();
            final String currentIpValue = (String) currentIpField.get(null);
            return new FileSnapshot(configPath, currentIpValue);
        }

        void restore() throws Exception {
            final Field configFileField = Directories.class.getDeclaredField("configFile");
            configFileField.setAccessible(true);
            configFileField.set(null, this.configFile.toFile());

            final Field currentIpField = PhotonEngine.class.getDeclaredField("currentIP");
            currentIpField.setAccessible(true);
            currentIpField.set(null, this.currentIp);
        }
    }
}

package niwer.photon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;

import niwer.photon.util.updater.UpdateChannel;
import niwer.photon.util.updater.UpdateFileType;

class DirectoriesTest {

    private String originalConfigContent;
    private boolean originalConfigExists;
    private byte[] originalLogoBytes;
    private boolean originalLogoExists;

    @AfterEach
    void restoreFiles() throws IOException {
        Directories.config = Directories.getConfig();

        final Path configPath = Directories.configFile.toPath();
        if (this.originalConfigExists) {
            Files.writeString(configPath, this.originalConfigContent);
        } else {
            Files.deleteIfExists(configPath);
        }

        final Path logoPath = Directories.LOGO_FILE.toPath();
        if (this.originalLogoExists) {
            Files.write(logoPath, this.originalLogoBytes);
        } else {
            Files.deleteIfExists(logoPath);
        }
    }

    @BeforeEach
    void snapshotFiles() throws IOException {
        final Path configPath = Directories.configFile.toPath();
        this.originalConfigExists = Files.exists(configPath);
        this.originalConfigContent = this.originalConfigExists ? Files.readString(configPath) : null;

        final Path logoPath = Directories.LOGO_FILE.toPath();
        this.originalLogoExists = Files.exists(logoPath);
        this.originalLogoBytes = this.originalLogoExists ? Files.readAllBytes(logoPath) : null;
    }

    @Test
    void getConfigFallsBackToDefaultWhenUnset() {
        Directories.config = null;

        var config = Directories.getConfig();

        assertNotNull(config);
        assertEquals(7070, config.webserver_port);
        assertEquals("niwer-engine", config.license_product_id);
        assertEquals(Boolean.TRUE, config.dbBackupEnabled());
        assertEquals(Boolean.TRUE, config.dbBackupOnStartup());
        assertEquals(1440L, config.dbBackupIntervalMinutes());
    }

    @Test
    void getPathForUpdateChannelUsesConfiguredMapping() {
        Directories.config = new Directories.NetworkConfig();

        assertEquals(
            Directories.BASE_DIR.getPath() + "/services_update/network-dev.jar",
            Directories.getPathForUpdateChannel(UpdateFileType.NETWORK, UpdateChannel.DEV)
        );
    }

    @Test
    void loadCreatesConfigFileAndParsesDefaultConfigWhenMissing() throws IOException {
        final Path configPath = Directories.configFile.toPath();
        Files.deleteIfExists(configPath);
        Directories.config = null;

        Directories.load();

        assertTrue(Files.exists(configPath));
        assertNotNull(Directories.config);
        assertEquals(7070, Directories.config.webserver_port);
        assertEquals("niwer-engine", Directories.config.license_product_id);
        assertEquals(Boolean.TRUE, Directories.config.dbBackupEnabled());
        assertEquals(Boolean.TRUE, Directories.config.dbBackupOnStartup());
        assertEquals(1440L, Directories.config.dbBackupIntervalMinutes());
    }

    @Test
    void loadBackfillsBackupDefaultsFromOlderPartialConfigs() throws IOException {
        final Path configPath = Files.createTempFile("photon-config-partial", ".json");
        Files.writeString(configPath, "{\"discord_bot_token\":\"token-123\"}");
        Directories.configFile = configPath.toFile();
        Directories.config = null;

        Directories.load();

        assertNotNull(Directories.config);
        assertEquals("token-123", Directories.config.discord_bot_token);
        assertEquals(Boolean.TRUE, Directories.config.dbBackupEnabled());
        assertEquals(Boolean.TRUE, Directories.config.dbBackupOnStartup());
        assertEquals(1440L, Directories.config.dbBackupIntervalMinutes());

        Files.deleteIfExists(configPath);
    }

    @Test
    void saveWritesTheCurrentConfigToDisk() throws IOException {
        final Directories.NetworkConfig config = new Directories.NetworkConfig();
        config.webserver_port = 9191;
        config.website_url = "https://example.test";
        Directories.config = config;

        Directories.save();

        final String content = Files.readString(Directories.configFile.toPath());
        assertTrue(content.contains("9191"));
        assertTrue(content.contains("https://example.test"));
    }

    @Test
    void getOfficialLogoBase64ReturnsDataForAValidPng() throws IOException {
        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(image, "png", Directories.LOGO_FILE);

        final String base64 = Directories.getOfficialLogoBase64();

        assertNotNull(base64);
        assertTrue(Base64.getDecoder().decode(base64).length > 0);
    }

    @Test
    void getOfficialLogoBase64ReturnsNullForInvalidImageData() throws IOException {
        Files.writeString(Directories.LOGO_FILE.toPath(), "not-an-image");

        assertNull(Directories.getOfficialLogoBase64());
    }
}

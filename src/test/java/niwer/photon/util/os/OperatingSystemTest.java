package niwer.photon.util.os;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class OperatingSystemTest {

    @Test
    void hashProducesExpectedSha1() {
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", OperatingSystem.hash("abc"));
        assertEquals("", OperatingSystem.hash("Photon", "NOPE"));
        assertEquals("unknown", OperatingSystem.hash((File) null, "SHA-1"));
    }

    @Test
    void getHWIDReturnsAStableHashString() {
        final String hwid = OperatingSystem.getHWID();

        assertNotNull(hwid);
        assertTrue(hwid.matches("[0-9a-f]*"));
    }

    @Test
    void loadFileCanReadBundledResources() {
        assertNotNull(OperatingSystem.loadFile("lang/lang_en.properties"));
        assertNull(OperatingSystem.loadFile("lang/does-not-exist.properties"));
    }

    @Test
    void currentPlatformAndWorkingDirectoryFollowSystemProperties() throws Exception {
        final String originalOsName = System.getProperty("os.name");
        final String originalUserHome = System.getProperty("user.home");
        final Path tempHome = Files.createTempDirectory("photon-home");

        try {
            System.setProperty("os.name", "Windows 11");
            System.setProperty("user.home", tempHome.toString());

            assertEquals(OperatingSystem.WINDOWS, OperatingSystem.currentPlatform());

            final File workingDirectory = OperatingSystem.getWorkingDirectory("PhotonTest");
            assertTrue(workingDirectory.exists());
            assertTrue(workingDirectory.getPath().contains("AppData"));
        } finally {
            if (originalOsName != null) System.setProperty("os.name", originalOsName);
            if (originalUserHome != null) System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void javaPathMatchesTheRunningPlatform() {
        final String javaPath = OperatingSystem.javaPath();

        assertNotNull(javaPath);
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            assertTrue(javaPath.startsWith("\"") && javaPath.endsWith("\\bin\\java\""));
        } else {
            assertTrue(javaPath.endsWith("/bin/java"));
        }
    }

    @Test
    void javaDirPrefersJavaWWhenPresentOnWindows() throws Exception {
        final String originalOsName = System.getProperty("os.name");
        final String originalJavaHome = System.getProperty("java.home");
        final Path tempJavaHome = Files.createTempDirectory("photon-java-home");
        final Path binDir = tempJavaHome.resolve("bin");
        Files.createDirectories(binDir);
        Files.writeString(binDir.resolve("javaw.exe"), "");

        try {
            System.setProperty("os.name", "Windows 11");
            System.setProperty("java.home", tempJavaHome.toString());

            assertTrue(OperatingSystem.currentPlatform() == OperatingSystem.WINDOWS);
            assertTrue(OperatingSystem.WINDOWS.javaDir().endsWith("javaw.exe"));
        } finally {
            if (originalOsName != null) System.setProperty("os.name", originalOsName);
            if (originalJavaHome != null) System.setProperty("java.home", originalJavaHome);
        }
    }

    @Test
    void currentPlatformFallsBackToUnknown() {
        final String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Plan9");
            assertEquals(OperatingSystem.UNKNOWN, OperatingSystem.currentPlatform());
        } finally {
            if (originalOsName != null) System.setProperty("os.name", originalOsName);
        }
    }
}

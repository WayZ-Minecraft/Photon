package com.niwer.util.os;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import niwer.photon.util.os.OperatingSystem;

public class OperatingSystemTest {

    @Test
    public void testOperatingSystemDetection() {
        String osName = System.getProperty("os.name").toLowerCase();
        OperatingSystem detectedOS = OperatingSystem.currentPlatform();
        assertEquals(osName, detectedOS.NAME, "Operating system detection failed");
    }

    @Test
    public void testJavaPath() {
        String javaPath = OperatingSystem.javaPath();
        String expectedPath = System.getProperty("java.home") + "/bin/java";
        if (OperatingSystem.currentPlatform() == OperatingSystem.WINDOWS)
            expectedPath = "\"" + System.getProperty("java.home") + "\\bin\\java" + "\"";

        assertEquals(expectedPath, javaPath, "Java path detection failed");
    }

    @Test
    public void testJavaDir() {
        String javaDir = OperatingSystem.currentPlatform().javaDir();
        String expectedDir = System.getProperty("java.home") + "/bin/java";
        if (OperatingSystem.currentPlatform() == OperatingSystem.WINDOWS)
            expectedDir = System.getProperty("java.home") + "\\bin\\javaw.exe";

        assertEquals(expectedDir, javaDir, "Java directory detection failed");
    }

    @Test
    public void testIsSupported() {
        OperatingSystem os = OperatingSystem.currentPlatform();
        if (os == OperatingSystem.UNKNOWN) assertEquals(false, os.isSupported(), "Unknown OS should not be supported");
        else assertEquals(true, os.isSupported(), "Known OS should be supported");
    }

    @Test
    public void testLoadFile() {
    }

    @Test
    public void testOperatingSystemAliases() {
        OperatingSystem os = OperatingSystem.currentPlatform();
        String osName = System.getProperty("os.name").toLowerCase();

        boolean aliasMatch = false;
        for (String alias : os.NAME_ALIASES) {
            if (osName.contains(alias)) {
                aliasMatch = true;
                break;
            }
        }

        assertEquals(true, aliasMatch, "Operating system alias detection failed");
    }

    @Test
    public void testGetHwid() {
        String hwid = OperatingSystem.getHWID();
        assertEquals(64, hwid.length(), "HWID has incorrect length"); //SHA-256 hash length is 64 characters
    }

    @Test
    public void testGetWorkingDirectory() {
        String workDirName = "TestConfig";
        java.io.File workingDir = OperatingSystem.getWorkingDirectory(workDirName);
        assertEquals(true, workingDir.exists(), "Working directory does not exist");
        assertEquals(true, workingDir.isDirectory(), "Working directory is not a directory");
    }
}

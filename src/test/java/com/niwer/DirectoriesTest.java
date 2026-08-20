package com.niwer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import niwer.photon.Directories;
import niwer.photon.util.updater.UpdateChannel;
import niwer.photon.util.updater.UpdateFileType;

public class DirectoriesTest {

    @Test
    public void testLoadDirectories() {
        Directories.load();
        assertTrue(Directories.BASE_DIR.exists());
        assertTrue(Directories.LOGS_DIR.exists());
        assertTrue(Directories.BACKUPS_DIR.exists());
        assertTrue(Directories.configFile.exists());
    }

    @Test
    public void testGetConfig() {
        Directories.load();
        assertTrue(Directories.getConfig() != null);
    }

    @Test
    public void testGetPathForUpdateChannel() {
        Directories.load();
        String path = Directories.getPathForUpdateChannel(UpdateFileType.MOD, UpdateChannel.STABLE);
            
        assertTrue(path != null && !path.isEmpty());
    }

    @Test
    public void testSaveConfig() {
        Directories.load();
        Directories.save();
        assertTrue(Directories.configFile.exists());
    }
}
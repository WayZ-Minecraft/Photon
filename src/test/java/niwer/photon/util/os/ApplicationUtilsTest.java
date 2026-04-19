package niwer.photon.util.os;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ApplicationUtilsTest {

    @Test
    void launchHandlesJarFiles() throws Exception {
        final Path jarFile = Files.createTempFile("photon-launch", ".jar");
        try {
            assertDoesNotThrow(() -> ApplicationUtils.launch(jarFile.toFile(), new String[0], false, 0L));
        } finally {
            Files.deleteIfExists(jarFile);
        }
    }

    @Test
    void launchHandlesDirectExecutables() {
        final String comSpec = System.getenv("ComSpec");
        if (comSpec == null || comSpec.isBlank()) return;

        assertDoesNotThrow(() -> ApplicationUtils.launch(new File(comSpec), new String[] { "/c", "exit", "0" }, false, 0L));
    }

    @Test
    void launchHandlesStartFailuresWithoutThrowing() {
        assertDoesNotThrow(() -> ApplicationUtils.launch(new File("this-file-does-not-exist.exe"), new String[0], false, 0L));
    }
}
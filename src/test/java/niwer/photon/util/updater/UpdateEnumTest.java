package niwer.photon.util.updater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UpdateEnumTest {

    @Test
    void updateChannelParsesCaseInsensitiveNames() {
        assertEquals(UpdateChannel.DEV, UpdateChannel.fromString("dev"));
        assertEquals(UpdateChannel.STABLE, UpdateChannel.fromString("STABLE"));
        assertThrows(IllegalArgumentException.class, () -> UpdateChannel.fromString("nightly"));
    }

    @Test
    void updateFileTypeParsesCaseInsensitiveNames() {
        assertEquals(UpdateFileType.NETWORK, UpdateFileType.fromString("network"));
        assertEquals(UpdateFileType.MOD, UpdateFileType.fromString("mod"));
        assertThrows(IllegalArgumentException.class, () -> UpdateFileType.fromString("plugin"));
    }
}

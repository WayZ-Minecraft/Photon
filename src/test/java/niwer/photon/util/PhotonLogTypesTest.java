package niwer.photon.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

class PhotonLogTypesTest {

    @Test
    void exposesTheExpectedStaticLogTypes() {
        assertNotNull(PhotonLogTypes.NETWORK);
        assertNotNull(PhotonLogTypes.SQL);
        assertNotNull(PhotonLogTypes.DISCORD_BOT);
        assertNotNull(PhotonLogTypes.LICENSE);

        assertNotSame(PhotonLogTypes.NETWORK, PhotonLogTypes.SQL);
        assertNotSame(PhotonLogTypes.SQL, PhotonLogTypes.DISCORD_BOT);
        assertNotSame(PhotonLogTypes.DISCORD_BOT, PhotonLogTypes.LICENSE);
    }
}

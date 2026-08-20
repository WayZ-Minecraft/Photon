package com.niwer.util;

import org.junit.jupiter.api.Test;

import niwer.photon.util.PhotonLogTypes;

public class PhotonLogTypesTest {

    @Test
    public void testSilenceLogsFor() {
        /* Test that the method does not throw an exception for a valid logger name */
        PhotonLogTypes.silenceLogsFor("niwer.photon.util.PhotonLogTypes");
    }
}

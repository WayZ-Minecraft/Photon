package com.niwer.util.os;

import org.junit.jupiter.api.Test;

import niwer.photon.util.os.ApplicationUtils;

public class ApplicationUtilsTest {

    @Test
    public void testRestart() {
        ApplicationUtils.restart(ApplicationUtilsTest.class, "arg1", "arg2");
        ApplicationUtils.restart(ApplicationUtilsTest.class, 2000L, new String[]{"arg1", "arg2"});
    }

    @Test
    public void testLaunch() {
        ApplicationUtils.launch(new java.io.File("test.jar"), new String[]{"arg1", "arg2"}, false, 1000L);
    }

    @Test
    public void testLaunchWithExit() {
        ApplicationUtils.launch(new java.io.File("test.jar"), new String[]{"arg1", "arg2"}, true, 1000L);
    }
}

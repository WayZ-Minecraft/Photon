package com.niwer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import niwer.photon.PhotonEngine;

public class PhotonEngineTest {

    @Test
    public void testGetDate() {
        String dateWithoutTime = PhotonEngine.getDate(false);
        String dateWithTime = PhotonEngine.getDate(true);

        // Check that the date without time has the correct format
        assertEquals(10, dateWithoutTime.length());
        assertTrue(dateWithoutTime.matches("\\d{2}-\\d{2}-\\d{4}"));

        // Check that the date with time has the correct format
        assertEquals(19, dateWithTime.length());
        assertTrue(dateWithTime.matches("\\d{2}-\\d{2}-\\d{4}_\\d{2}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testGetCurrentIP() {
        String currentIP = PhotonEngine.getCurrentIP();
        assertTrue(currentIP != null && !currentIP.isBlank(), "Current IP should not be null or blank");
    }

    @Test
    public void testIsIPEquals() {
        String currentIP = PhotonEngine.getCurrentIP();
        assertTrue(PhotonEngine.isIPEquals(currentIP), "isIPEquals should return true for the current IP");
    }

    @Test
    public void testIsOnline() {
        // try {
        //     assertTrue(PhotonEngine.isOnline("google.com"), "Google's DNS should be online");
        // } catch (Exception e) {
        //     fail("Failed to check if IP is online");
        // }
    }
}

package com.niwer.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import niwer.photon.util.HashUtils;

public class HashUtilsTest {

    @Test
    public void testHashPassword() {
        String password = "testPassword";
        String hashedPassword = HashUtils.hashPassword(password);
        assertNotNull(hashedPassword);
        assertTrue(HashUtils.passwordMatches(hashedPassword, password));

        /* Test with null and empty passwords */
        assertNull(HashUtils.hashPassword(null));
        assertNull(HashUtils.hashPassword(""));
    }
   
    @Test
    public void testPasswordMatches() {
        String password = "testPassword";
        String hashedPassword = HashUtils.hashPassword(password);
        assertTrue(HashUtils.passwordMatches(hashedPassword, password));

        /* Test with non Argon2 */
        // assertFalse(HashUtils.passwordMatches("testPassword", password));

        /* Test with null and empty passwords */
        assertTrue(!HashUtils.passwordMatches(null, password));
        assertTrue(!HashUtils.passwordMatches(hashedPassword, null));
        assertTrue(!HashUtils.passwordMatches("", password));
        assertTrue(!HashUtils.passwordMatches(hashedPassword, ""));
    }

    @Test
    public void testIsArgon2Hash() {
        String password = "testPassword";
        String hashedPassword = HashUtils.hashPassword(password);
        assertTrue(hashedPassword.startsWith("$argon2"));
        assertTrue(HashUtils.isArgon2Hash(hashedPassword));

        /* Test with non Argon2 hash */
        assertFalse(HashUtils.isArgon2Hash("testPassword"));

        /* Test with null hash */
        assertFalse(HashUtils.isArgon2Hash(null));
    }

    @Test
    public void testHashFile() {
        /* Test with null file */
        assertThrows(IllegalArgumentException.class, () -> HashUtils.hashFile(null));
        assertThrows(IllegalArgumentException.class, () -> HashUtils.hashFile(Path.of("nonexistentfile.txt")));
    }

    @Test
    public void testHashString() {
        String input = "testString";
        String hash = HashUtils.hash(input);
        assertNotNull(hash);

        /* Test with null input */
        assertNull(HashUtils.hash(null));
    }
}

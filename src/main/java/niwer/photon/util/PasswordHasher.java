package niwer.photon.util;

import java.util.Arrays;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public final class PasswordHasher {

    private static final int ITERATIONS = 3;
    private static final int MEMORY_KIB = 65536;
    private static final int PARALLELISM = 1;
    private static final String ARGON2_PREFIX = "$argon2";

    private PasswordHasher() {}

    public static String hash(String password) {
        if (password == null || password.isBlank()) return null;

        final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        final char[] passwordChars = password.toCharArray();
        try {
            return argon2.hash(ITERATIONS, MEMORY_KIB, PARALLELISM, passwordChars);
        } finally {
            Arrays.fill(passwordChars, '\0');
            argon2.wipeArray(passwordChars);
        }
    }

    public static boolean matches(String storedPassword, String rawPassword) {
        if (storedPassword == null || storedPassword.isBlank() || rawPassword == null || rawPassword.isBlank()) return false;

        if (isArgon2Hash(storedPassword)) {
            final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
            final char[] passwordChars = rawPassword.toCharArray();
            try {
                return argon2.verify(storedPassword, passwordChars);
            } finally {
                Arrays.fill(passwordChars, '\0');
                argon2.wipeArray(passwordChars);
            }
        }

        return storedPassword.equals(rawPassword);
    }

    public static boolean isArgon2Hash(String value) {
        return value != null && value.startsWith(ARGON2_PREFIX);
    }
}

package niwer.photon.util.updater;

public enum UpdateChannel {
    STABLE, ALPHA, BETA, DEV, TEST;

    public static UpdateChannel fromString(String value) {
        for (final UpdateChannel CHANNEL : UpdateChannel.values()) {
            if (CHANNEL.name().equalsIgnoreCase(value)) return CHANNEL;
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }

    public static boolean isVariant(String channel) {
        channel = channel.toUpperCase(); // Ensure we're in uppercase to prevent case sensitivity bugs

        try {
            return fromString(channel) != null;
        } catch(Exception e) {
            return false;
        }
    }
}

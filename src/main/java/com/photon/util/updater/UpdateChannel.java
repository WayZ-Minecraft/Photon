package com.photon.util.updater;

public enum UpdateChannel {
    STABLE, ALPHA, BETA, DEV, TEST;

    public static UpdateChannel fromString(String value) {
        for (UpdateChannel channel : UpdateChannel.values()) {
            if (channel.name().equalsIgnoreCase(value)) return channel;
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}

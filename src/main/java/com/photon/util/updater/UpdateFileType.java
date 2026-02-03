package com.photon.util.updater;

public enum UpdateFileType {
    MOD, LAUNCHER, API, NETWORK;

    public static UpdateFileType fromString(String value) {
        for (UpdateFileType type : UpdateFileType.values()) {
            if (type.name().equalsIgnoreCase(value)) return type;
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}

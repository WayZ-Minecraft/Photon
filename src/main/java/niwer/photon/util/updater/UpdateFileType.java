package niwer.photon.util.updater;

public enum UpdateFileType {
    MOD, LAUNCHER, API, NETWORK;

    public static UpdateFileType fromString(String value) {
        for (final UpdateFileType FILE_TYPE : UpdateFileType.values()) {
            if (FILE_TYPE.name().equalsIgnoreCase(value)) return FILE_TYPE;
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}

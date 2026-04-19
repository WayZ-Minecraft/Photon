package niwer.photon.discord;

import niwer.lumen.Console;

public final class BotEngine {

    private static boolean loadCalled;
    public static boolean lastRestartValue;

    private BotEngine() {}

    public static void reset() {
        loadCalled = false;
        lastRestartValue = false;
    }

    public static void load(boolean shouldRestart) {
        loadCalled = true;
        lastRestartValue = shouldRestart;
    }

    public static boolean wasLoadCalled() {
        return loadCalled;
    }

    public static boolean lastRestartValue() {
        return lastRestartValue;
    }

    public static boolean isBotInitialized() {
        return false;
    }

    public static void log(Console data) {}
}
package io.javalin;

import java.util.function.Consumer;

import io.javalin.config.JavalinConfig;

public final class Javalin {

    private static JavalinConfig lastConfig;
    private static Integer lastStartedPort;

    private Javalin() {}

    public static Javalin create(Consumer<JavalinConfig> configConsumer) {
        final JavalinConfig config = new JavalinConfig();
        lastConfig = config;
        configConsumer.accept(config);
        return new Javalin();
    }

    public void start(int port) {
        lastStartedPort = port;
    }

    public static void reset() {
        lastConfig = null;
        lastStartedPort = null;
    }

    public static JavalinConfig lastConfig() {
        return lastConfig;
    }

    public static Integer lastStartedPort() {
        return lastStartedPort;
    }
}
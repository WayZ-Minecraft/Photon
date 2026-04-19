package io.javalin.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.javalin.http.Context;

public final class JavalinConfig {

    public final Routes routes = new Routes();
    public final StaticFiles staticFiles = new StaticFiles();

    public static final class Routes {
        public final List<RouteRegistration> registrations = new ArrayList<>();

        public void get(String path, Consumer<Context> handler) { registrations.add(new RouteRegistration("GET", path, handler)); }
        public void post(String path, Consumer<Context> handler) { registrations.add(new RouteRegistration("POST", path, handler)); }
        public void put(String path, Consumer<Context> handler) { registrations.add(new RouteRegistration("PUT", path, handler)); }
        public void delete(String path, Consumer<Context> handler) { registrations.add(new RouteRegistration("DELETE", path, handler)); }
    }

    public static final class StaticFiles {
        public final List<String> addedDirectories = new ArrayList<>();

        public void add(String directory) { addedDirectories.add(directory); }
    }

    public record RouteRegistration(String method, String path, Consumer<Context> handler) {}
}
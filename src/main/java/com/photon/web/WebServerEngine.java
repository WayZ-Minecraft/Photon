package com.photon.web;

import org.slf4j.LoggerFactory;

import com.photon.network.NetworkDirectories;
import com.photon.util.NetworkOnly;

import ch.qos.logback.classic.Logger;
import io.javalin.Javalin;

@NetworkOnly
public class WebServerEngine {

    public static void load() {
        /* Change debug level */
        {
            final Logger JAVALIN_LOGGER = (Logger) LoggerFactory.getLogger("io.javalin");
            JAVALIN_LOGGER.setLevel(ch.qos.logback.classic.Level.WARN);
    
            final Logger JETTY_LOGGER = (Logger) LoggerFactory.getLogger("org.eclipse.jetty");
            JETTY_LOGGER.setLevel(ch.qos.logback.classic.Level.WARN);
        }

        /* Create the web server */
        final var WEB_SERVER = Javalin.create(cfg -> {
            /* Set the static files directory (index.html, main.css, main.js, etc.) */
            cfg.staticFiles.add("/public");
            
            //TODO
            cfg.routes.get("/status", ctx -> ctx.result("Hello World"));

            cfg.routes.post("/restart", ctx -> {
                ctx.status(200).result("Restarting...");
            });

            // cfg.routes.post("/create_account", ctx -> {
            //     final String username = ctx.formParam("username");
            //     final String email = ctx.formParam("email");
            //     final String password = ctx.formParam("password");

            //     if(username == null || email == null || password == null) {
            //         ctx.status(400).result("Missing parameters");
            //         return;
            //     }

            //     SQLACcountManager.createAccount(username, email, password);

            //     ctx.status(200).result("Account created successfully.");
            // });
        });

        /* Start the web server */
        WEB_SERVER.start(NetworkDirectories.getConfig().webserver_port);
    }

    /**
     * Features to add :
     * - Public Status (Servers) page
     * 
     * - Server management page (with authentication) -> The hard part
     * 
     * - Network panel (with authentication with project creator)
     * - News management page (with authentication)
     * - Network Logo upload page (with authentication)
     * 
     * - Update from webserver (with authentication)
     * (Every bot command should be available from webserver)
     */
}

package com.photon.web;

import org.slf4j.LoggerFactory;

import com.photon.Directories;
import com.photon.util.NetworkOnly;
import com.photon.web.endpoints.IEndpoint;
import com.photon.web.endpoints.AddServerEndpoint;
import com.photon.web.endpoints.NetworkConfigEndpoint;
import com.photon.web.endpoints.RestartEndpoint;
import com.photon.web.endpoints.ServerListEndpoint;
import com.photon.web.endpoints.news.NewsListEndpoint;
import com.photon.web.endpoints.accounts.CreateAccountEndpoint;
import com.photon.web.endpoints.tebex.LicenseEndpoint;

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
            
            /* Endpoints */
            IEndpoint.register(cfg, RestartEndpoint.class);
            IEndpoint.register(cfg, LicenseEndpoint.class);
            IEndpoint.register(cfg, CreateAccountEndpoint.class);
            IEndpoint.register(cfg, AddServerEndpoint.class);
            IEndpoint.register(cfg, NewsListEndpoint.class);
            IEndpoint.register(cfg, ServerListEndpoint.class);
            IEndpoint.register(cfg, NetworkConfigEndpoint.class);
        });

        /* Start the web server */
        WEB_SERVER.start(Directories.getConfig().webserver_port);
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

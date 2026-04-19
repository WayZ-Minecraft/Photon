package niwer.photon.web;

import org.slf4j.LoggerFactory;

import niwer.photon.Directories;
import niwer.photon.web.endpoints.AddAntiCheatReportEndpoint;
import niwer.photon.web.endpoints.AddCrashReportEndpoint;
import niwer.photon.web.endpoints.AddHWIDEndpoint;
import niwer.photon.web.endpoints.IEndpoint;
import niwer.photon.web.endpoints.NetworkConfigEndpoint;
import niwer.photon.web.endpoints.UpdateEndpoint;
import niwer.photon.web.endpoints.RestartEndpoint;
import niwer.photon.web.endpoints.news.NewsListEndpoint;
import niwer.photon.web.endpoints.servers.AddServerEndpoint;
import niwer.photon.web.endpoints.servers.ServerListEndpoint;
import niwer.photon.web.endpoints.accounts.CreateAccountEndpoint;
import niwer.photon.web.endpoints.tebex.LicenseEndpoint;

import ch.qos.logback.classic.Logger;
import io.javalin.Javalin;

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
            IEndpoint.register(cfg, NetworkConfigEndpoint.class);
            IEndpoint.register(cfg, AddCrashReportEndpoint.class);
            IEndpoint.register(cfg, AddAntiCheatReportEndpoint.class);
            IEndpoint.register(cfg, AddHWIDEndpoint.class);
            IEndpoint.register(cfg, UpdateEndpoint.class);
            {
                /* Tebex */
                IEndpoint.register(cfg, LicenseEndpoint.class);
            }
            {
                /* Accounts */
                IEndpoint.register(cfg, CreateAccountEndpoint.class);
            }
            {
                /* Servers */
                IEndpoint.register(cfg, AddServerEndpoint.class);
                IEndpoint.register(cfg, ServerListEndpoint.class);
            }
            {
                /* News */
                IEndpoint.register(cfg, NewsListEndpoint.class);
            }
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

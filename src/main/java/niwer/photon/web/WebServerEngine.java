package niwer.photon.web;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import io.javalin.Javalin;
import niwer.photon.Directories;
import niwer.photon.web.endpoints.AddAntiCheatReportEndpoint;
import niwer.photon.web.endpoints.AddCrashReportEndpoint;
import niwer.photon.web.endpoints.AddHWIDEndpoint;
import niwer.photon.web.endpoints.HomeEndpoint;
import niwer.photon.web.endpoints.IEndpoint;
import niwer.photon.web.endpoints.NetworkConfigEndpoint;
import niwer.photon.web.endpoints.StatusServersEndpoint;
import niwer.photon.web.endpoints.accounts.AuthAccountEndpoint;
import niwer.photon.web.endpoints.accounts.ChangePasswordEndpoint;
import niwer.photon.web.endpoints.accounts.CreateAccountEndpoint;
import niwer.photon.web.endpoints.accounts.UpdateProfileEndpoint;
import niwer.photon.web.endpoints.admin.AdminConfigEndpoint;
import niwer.photon.web.endpoints.admin.AdminLoginEndpoint;
import niwer.photon.web.endpoints.admin.AdminMeEndpoint;
import niwer.photon.web.endpoints.admin.AdminRestartEndpoint;
import niwer.photon.web.endpoints.admin.AdminTableDataEndpoint;
import niwer.photon.web.endpoints.admin.AdminTablesEndpoint;
import niwer.photon.web.endpoints.admin.AdminUpdateConfigEndpoint;
import niwer.photon.web.endpoints.admin.AdminUpdateEndpoint;
import niwer.photon.web.endpoints.servers.AddServerEndpoint;
import niwer.photon.web.endpoints.servers.ServerListEndpoint;
import niwer.photon.web.endpoints.tebex.LicenseEndpoint;

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
            IEndpoint.register(cfg, HomeEndpoint.class);
            IEndpoint.register(cfg, StatusServersEndpoint.class);
            IEndpoint.register(cfg, NetworkConfigEndpoint.class);
            IEndpoint.register(cfg, AddCrashReportEndpoint.class);
            IEndpoint.register(cfg, AddAntiCheatReportEndpoint.class);
            IEndpoint.register(cfg, AddHWIDEndpoint.class);
            IEndpoint.register(cfg, AdminUpdateEndpoint.class);
            {
                /* Admin panel */
                IEndpoint.register(cfg, AdminLoginEndpoint.class);
                IEndpoint.register(cfg, AdminMeEndpoint.class);
                IEndpoint.register(cfg, AdminConfigEndpoint.class);
                IEndpoint.register(cfg, AdminUpdateConfigEndpoint.class);
                IEndpoint.register(cfg, AdminRestartEndpoint.class);
                IEndpoint.register(cfg, AdminTablesEndpoint.class);
                IEndpoint.register(cfg, AdminTableDataEndpoint.class);
            }
            {
                /* Tebex */
                IEndpoint.register(cfg, LicenseEndpoint.class);
            }
            {
                /* Accounts */
                IEndpoint.register(cfg, CreateAccountEndpoint.class);
                IEndpoint.register(cfg, AuthAccountEndpoint.class);
                IEndpoint.register(cfg, ChangePasswordEndpoint.class);
                IEndpoint.register(cfg, UpdateProfileEndpoint.class);
            }
            {
                /* Servers */
                IEndpoint.register(cfg, AddServerEndpoint.class);
                IEndpoint.register(cfg, ServerListEndpoint.class);
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
     * - Network Logo upload page (with authentication)
     * 
     * - Update from webserver (with authentication)
     * (Every bot command should be available from webserver)
     */
}

package niwer.photon.web;

import org.eclipse.jetty.server.ForwardedRequestCustomizer;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.RateLimitPlugin;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.util.session.AdminSessionManager;
import niwer.photon.util.session.UserSessionManager;
import niwer.photon.web.endpoints.HomeEndpoint;
import niwer.photon.web.endpoints.IEndpoint;
import niwer.photon.web.endpoints.LicenseValidateEndpoint;
import niwer.photon.web.endpoints.accounts.AuthAccountEndpoint;
import niwer.photon.web.endpoints.accounts.ChangePasswordEndpoint;
import niwer.photon.web.endpoints.accounts.CreateAccountEndpoint;
import niwer.photon.web.endpoints.accounts.UpdateProfileEndpoint;
import niwer.photon.web.endpoints.accounts.UserMeEndpoint;
import niwer.photon.web.endpoints.accounts.licenses.AccountLicenseCreateEndpoint;
import niwer.photon.web.endpoints.accounts.licenses.AccountLicenseListEndpoint;
import niwer.photon.web.endpoints.accounts.licenses.AccountLicenseRevokeEndpoint;
import niwer.photon.web.endpoints.admin.AdminConfigEndpoint;
import niwer.photon.web.endpoints.admin.AdminLoginEndpoint;
import niwer.photon.web.endpoints.admin.AdminMeEndpoint;
import niwer.photon.web.endpoints.admin.AdminRestartEndpoint;
import niwer.photon.web.endpoints.admin.AdminTableDataEndpoint;
import niwer.photon.web.endpoints.admin.AdminTablesEndpoint;
import niwer.photon.web.endpoints.admin.AdminUpdateConfigEndpoint;
import niwer.photon.web.endpoints.admin.AdminUpdateEndpoint;
import niwer.photon.web.endpoints.admin.AdminUploadUpdateEndpoint;
import niwer.photon.web.endpoints.game.AddAntiCheatReportEndpoint;
import niwer.photon.web.endpoints.game.AddCrashReportEndpoint;
import niwer.photon.web.endpoints.game.AddHWIDEndpoint;
import niwer.photon.web.endpoints.game.InfoEndpoint;
import niwer.photon.web.endpoints.game.ModDownloadEndpoint;
import niwer.photon.web.endpoints.servers.AddServerEndpoint;
import niwer.photon.web.endpoints.servers.ServerListEndpoint;
import niwer.photon.web.endpoints.servers.StatusServersEndpoint;
import niwer.photon.web.endpoints.stripe.StripePurchaseSessionEndpoint;
import niwer.photon.web.endpoints.stripe.StripeWebhookEndpoint;

public class WebServerEngine {

    public static void load() {
        Thread thread = new Thread(() -> {
            /* Change debug level */
            {
                PhotonLogTypes.silenceLogsFor("io.javalin");
                PhotonLogTypes.silenceLogsFor("org.eclipse.jetty");
            }

            /* Load admin sessions */
            AdminSessionManager.load();
            UserSessionManager.load();

            /* Create the web server */
            final var WEB_SERVER = Javalin.create(cfg -> {
                /* Set the static files directory (index.html, main.css, main.js, etc.) */
                cfg.staticFiles.add("/public");

                /* Register plugins */
                cfg.registerPlugin(new RateLimitPlugin(plugin_cfg -> {
                    plugin_cfg.setKeyFunction(ctx -> ctx.ip() + "-" + ctx.path()); // Use IP and path as the key for rate limiting
                }));

                /* Endpoints */
                IEndpoint.register(cfg, HomeEndpoint.class);
                IEndpoint.register(cfg, StatusServersEndpoint.class);
                IEndpoint.register(cfg, InfoEndpoint.class);
                IEndpoint.register(cfg, AddCrashReportEndpoint.class);
                IEndpoint.register(cfg, AddAntiCheatReportEndpoint.class);
                IEndpoint.register(cfg, AddHWIDEndpoint.class);
                IEndpoint.register(cfg, LicenseValidateEndpoint.class);
                IEndpoint.register(cfg, AdminUpdateEndpoint.class);
                IEndpoint.register(cfg, ModDownloadEndpoint.class);
                {
                    /* Admin panel */
                    IEndpoint.register(cfg, AdminLoginEndpoint.class);
                    IEndpoint.register(cfg, AdminMeEndpoint.class);
                    IEndpoint.register(cfg, AdminConfigEndpoint.class);
                    IEndpoint.register(cfg, AdminUpdateConfigEndpoint.class);
                    IEndpoint.register(cfg, AdminUploadUpdateEndpoint.class);
                    IEndpoint.register(cfg, AdminRestartEndpoint.class);
                    IEndpoint.register(cfg, AdminTablesEndpoint.class);
                    IEndpoint.register(cfg, AdminTableDataEndpoint.class);
                }
                {
                    /* Stripe */
                    IEndpoint.register(cfg, StripePurchaseSessionEndpoint.class);
                    IEndpoint.register(cfg, StripeWebhookEndpoint.class);
                }
                {
                    /* Accounts */
                    IEndpoint.register(cfg, CreateAccountEndpoint.class);   
                    IEndpoint.register(cfg, AuthAccountEndpoint.class);
                    IEndpoint.register(cfg, UserMeEndpoint.class);
                    IEndpoint.register(cfg, ChangePasswordEndpoint.class);
                    IEndpoint.register(cfg, UpdateProfileEndpoint.class);
                    IEndpoint.register(cfg, AccountLicenseListEndpoint.class);
                    IEndpoint.register(cfg, AccountLicenseCreateEndpoint.class);
                    IEndpoint.register(cfg, AccountLicenseRevokeEndpoint.class);
                }
                {
                    /* Servers */
                    IEndpoint.register(cfg, AddServerEndpoint.class);
                    IEndpoint.register(cfg, ServerListEndpoint.class);
                }

                /* Force Configure Jetty */
                cfg.jetty.host = Directories.getConfig().webserver_host;
                cfg.jetty.port = Directories.getConfig().webserver_port;
                cfg.jetty.modifyHttpConfiguration(httpConfig -> httpConfig.addCustomizer(new ForwardedRequestCustomizer()));
            });

            /* Start the web server */
            WEB_SERVER.start(Directories.getConfig().webserver_host, Directories.getConfig().webserver_port);

            Console.log(getServingBox(Directories.getConfig().webserver_port)).container(PhotonEngine.LOGGER).type(PhotonLogTypes.WEB_SERVER).send();
        }, "Web Server Thread");
        thread.start();
    }

    /**
     * Get a printable serving box, so we can easily find where the web server is hosted
     * 
     * @param port The port of the web server
     * @return The printable box as String
     */
    private static String getServingBox(int port) {
        final StringBuilder BUILDER = new StringBuilder();

        /* Prepare the box content */
        final String PADDING = "    ";
        final String LINE_1 = PADDING + "Serving!";
        final String LINE_2 = PADDING + String.format("- Local:    http://localhost:%d", port);
        final String LINE_3 = PADDING + String.format("- Network:  %s:%d", "http://192.168.x.x", port);

        /* Find the longest line to determine the box width */
        /* '3' is a little extra to the right side */
        int maxWidth = getLineMaxWidth(LINE_1, LINE_2, LINE_3) + 3;

        /* Print the box */
        BUILDER.append("\n"); // Default go to next line because Lumen will add text like [Class:233] [00:00:00] [WEB]
        BUILDER.append("┌" + "─".repeat(maxWidth) + "┐\n");
        BUILDER.append(addBordersToLine(LINE_1, maxWidth)).append("\n");
        BUILDER.append(addBordersToLine(LINE_2, maxWidth)).append("\n");
        BUILDER.append(addBordersToLine(LINE_3, maxWidth)).append("\n");
        BUILDER.append("└" + "─".repeat(maxWidth) + "┘");
        return BUILDER.toString();
    }

    /**
     * Pads the right side of the string with spaces up to the required width
     */
    private static String addBordersToLine(String line, int width) {
        return "│" + String.format("%-" + width + "s", line) + "│";
    }

    private static int getLineMaxWidth(String... lines) {
        int width = 1;
        
        /* Check for each line if it's longer or not, if longer then update 'width' */
        for (final String LINE : lines) {
            final int LENGTH = LINE.length();
            if (LENGTH > width) width = LENGTH;
        }

        return width;
    }
}

package niwer.photon.web.endpoints.game;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.Directories.NetworkConfig;
import niwer.photon.PhotonEngine;
import niwer.photon.web.endpoints.IEndpoint;

public class InfoEndpoint implements IEndpoint {

    @Override public String path() { return "/game/config"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        final NetworkConfig CONFIG = Directories.getConfig();
        handler.json(new Config(CONFIG));
    }

    private record Config(
        String official_logo_base64,
        String discord_bot_id,

        // String api_version,
        String mod_version,
        // String launcher_version,

        String network_ip,
        int webserver_port,

        String twitter_url,
        String twitch_url,
        String youtube_url,
        String discord_url,
        String website_url
    ) {
        public Config(NetworkConfig config) {
            this(
                Directories.getOfficialLogoBase64(),
                config.discord_bot_id,

                // config.api_version,
                config.mod_version,
                // config.launcher_version,

                PhotonEngine.getCurrentIP(),
                config.webserver_port,

                config.twitter_url,
                config.twitch_url,
                config.youtube_url,
                config.discord_url,
                config.website_url
            );
        }
    }
}
package com.photon.web.endpoints;

import com.photon.Directories;
import com.photon.Directories.NetworkConfig;

import io.javalin.http.Context;

public class NetworkConfigEndpoint implements IEndpoint {

    @Override public String path() { return "/api/network-config"; }

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

                config.twitter_url,
                config.twitch_url,
                config.youtube_url,
                config.discord_url,
                config.website_url
            );
        }
    }
}
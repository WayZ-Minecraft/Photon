package niwer.photon.web.endpoints.admin;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminUpdateConfigEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/config"; }

    @Override public HttpMethod method() { return HttpMethod.PUT; }

    @Override
    public void handle(Context handler) {
        if (AdminSessionManager.requireAdministrator(handler) == null) return;
        if (!AdminSessionManager.validateCsrf(handler)) { handler.status(403).result("Invalid CSRF token"); return; }

        final ConfigUpdateRequest request;
        try {
            request = Directories.GSON.fromJson(handler.body(), ConfigUpdateRequest.class);
        } catch (Exception e) {
            handler.status(400).result("Invalid config payload");
            return;
        }

        if (request == null) {
            handler.status(400).result("Invalid config payload");
            return;
        }

        final var config = Directories.getConfig();
        if (request.bot_activity != null) config.bot_activity = request.bot_activity;
        if (request.discord_bot_token != null) config.discord_bot_token = request.discord_bot_token;
        if (request.discord_bot_id != null) config.discord_bot_id = request.discord_bot_id;
        if (request.official_discord_server_id != null) config.official_discord_server_id = request.official_discord_server_id;
        if (request.network_console_channel_id != null) config.network_console_channel_id = request.network_console_channel_id;
        if (request.server_creator_role_id != null) config.server_creator_role_id = request.server_creator_role_id;
        if (request.webserver_port != null) config.webserver_port = request.webserver_port;
        if (request.license_product_id != null) config.license_product_id = request.license_product_id;
        if (request.subscription_price_id != null) config.subscription_price_id = request.subscription_price_id;
        if (request.stripe_api_key != null) config.stripe_api_key = request.stripe_api_key;
        if (request.stripe_webhook_secret != null) config.stripe_webhook_signature = request.stripe_webhook_secret;
        if (request.license_default_duration_days != null) config.license_default_duration_days = request.license_default_duration_days;
        if (request.api_version != null) config.api_version = request.api_version;
        if (request.mod_version != null) config.mod_version = request.mod_version;
        if (request.launcher_version != null) config.launcher_version = request.launcher_version;
        if (request.twitter_url != null) config.twitter_url = request.twitter_url;
        if (request.twitch_url != null) config.twitch_url = request.twitch_url;
        if (request.youtube_url != null) config.youtube_url = request.youtube_url;
        		if (request.discord_url != null) config.discord_url = request.discord_url;
        		if (request.website_url != null) config.website_url = request.website_url;
        		if (request.store_url != null) config.store_url = request.store_url;
        		if (request.terms_of_service_url != null) config.terms_of_service_url = request.terms_of_service_url;
        		if (request.terms_of_sale_url != null) config.terms_of_sale_url = request.terms_of_sale_url;
        		if (request.privacy_policy_url != null) config.privacy_policy_url = request.privacy_policy_url;

        Directories.save();
        handler.json(Directories.getConfig());
    }

    	public record ConfigUpdateRequest(
            String bot_activity,
            String discord_bot_token,
            String discord_bot_id,
            String official_discord_server_id,
            String network_console_channel_id,
            String server_creator_role_id,
            Integer webserver_port,
            String license_product_id,
            String subscription_price_id,
            String stripe_api_key,
            String stripe_webhook_secret,
            Long license_default_duration_days,
            String api_version,
            String mod_version,
            String launcher_version,
            String twitter_url,
            String twitch_url,
            String youtube_url,
            String discord_url,
            String website_url,
            String store_url,
            String terms_of_service_url,
            String terms_of_sale_url,
            String privacy_policy_url
        ) {}
}
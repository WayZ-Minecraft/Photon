package niwer.photon.web.api;

import java.net.URI;
import java.net.http.HttpRequest;

import niwer.photon.Directories;
import niwer.photon.util.GsonUtils;

/**
 * Abstract class representing a GitHub API request. This class provides methods to prepare the request and body, as well as to sanitize input strings.
 * 
 * @author Niwer
 */
public abstract class ApiRequest {

    /**
     * Send the request to the server
     */
    public abstract void request();

    /**
     * Get the URL of the request
     * 
     * @return The URL of the request
     */
    public abstract String url();

    private final URI toURI() { return URI.create(url()); }

    private final String body() { return GsonUtils.GSON.toJson(this); }

    /**
     * Prepare the HttpRequest.Builder with the necessary headers and URI
     * 
     * @return The prepared HttpRequest.Builder
     */
    public final HttpRequest.Builder prepareRequest() {
        return HttpRequest.newBuilder()
            .uri(this.toURI())
            .header("Authorization", "Bearer " + Directories.getConfig().github_pat)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2026-03-10")
            .header("Content-Type", "application/json")
            .header("User-Agent", "Photon-Backend");
    }

    /**
     * Prepare the body of the request as a BodyPublisher
     * 
     * @return The prepared BodyPublisher
     */
    public final HttpRequest.BodyPublisher prepareBody() {
        return HttpRequest.BodyPublishers.ofString(this.body());
    }

    /**
     * Sanitize a string by removing all non-alphanumeric characters and converting it to lowercase
     * 
     * @param input The string to sanitize
     * @return The sanitized string
     */
    protected String sanitizeAndLower(String input) { return input.replaceAll("[^a-zA-Z0-9-_]", "").toLowerCase(); }
}

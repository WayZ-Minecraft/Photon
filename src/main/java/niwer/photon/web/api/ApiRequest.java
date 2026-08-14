package niwer.photon.web.api;

import java.net.URI;
import java.net.http.HttpRequest;

import niwer.photon.util.GsonUtils;
import niwer.photon.web.HttpMethod;

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

    /**
     * Get the HTTP method of the request
     * 
     * @return The HTTP method of the request
     */
    public abstract HttpMethod method();

    /**
     * Add necessary headers to the HttpRequest.Builder. This method should be implemented by subclasses to add any specific headers required for the request.
     * 
     * @param builder The HttpRequest.Builder to which headers should be added
     */
    public abstract void addHeaders(HttpRequest.Builder builder);
    
    private final URI toURI() { return URI.create(url()); }

    private final HttpRequest.BodyPublisher prepareBody() {
        return HttpRequest.BodyPublishers.ofString(GsonUtils.GSON.toJson(this));
    }

    /**
     * Prepare the HttpRequest.Builder with the necessary headers and URI
     * 
     * @return The prepared HttpRequest.Builder
     */
    public final HttpRequest asRequest() {
        final HttpRequest.Builder builder = HttpRequest.newBuilder().uri(this.toURI()).header("User-Agent", "Photon-Backend");
        this.addHeaders(builder); // Add any additional headers defined in the subclass

        switch (this.method()) {
            case GET -> builder.GET();
            case POST -> builder.POST(this.prepareBody());
            case PUT -> builder.PUT(this.prepareBody());
            case DELETE -> builder.DELETE();
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + this.method());
        }

        return builder.build();
    }

    /**
     * Sanitize a string by removing all non-alphanumeric characters and converting it to lowercase
     * 
     * @param input The string to sanitize
     * @return The sanitized string
     */
    protected String sanitizeAndLower(String input) { return input.replaceAll("[^a-zA-Z0-9-_]", "").toLowerCase(); }
}

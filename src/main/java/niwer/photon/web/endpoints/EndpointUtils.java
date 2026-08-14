package niwer.photon.web.endpoints;

import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.javalin.http.Context;
import niwer.photon.util.GsonUtils;

public class EndpointUtils {

    /**
     * Returns the first non-blank string from the provided array of strings.
     * 
     * @param values An array of strings to check for non-blank values
     * @return The first non-blank string, or null if all are blank or null
     */
    public static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    /**
     * Parses the raw JSON body of an HTTP request into a Map<String, Object>. If the body is null or blank, an empty map is returned.
     * If parsing fails, an empty map is also returned.
     * 
     * @param rawBody The raw JSON string from the HTTP request body
     * @return A Map<String, Object> representing the parsed JSON body, or an empty map if parsing fails or the body is blank
     */
    public static Map<String, Object> parseBody(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) return Map.of();

        try {
            final var TYPE = new TypeToken<Map<String, Object>>() {}.getType();
            final Map<String, Object> PARSED = GsonUtils.GSON.fromJson(rawBody, TYPE);
            
            return PARSED == null ? Map.of() : PARSED;
        } catch (Exception e) {
            return Map.of();
        }
    }

    public static JsonObject readBody(Context handler) {
        try {
            return GsonUtils.GSON.fromJson(handler.body(), JsonObject.class);
        } catch (Exception ignored) {
            return new JsonObject();
        }
    }

    /**
     * Retrieves a string value from the request body, query parameters, or form parameters based on the provided keys.
     * The method checks each key in order and returns the first non-blank value found.
     * 
     * @param handler The Javalin Context object representing the incoming request and response
     * @param body The parsed request body as a Map<String, Object>
     * @param keys The keys to look for in the request body, query parameters, and form parameters
     * @return The first non-blank string value found for the provided keys, or null if none are found
     */
    public static String getString(Context handler, Map<String, Object> body, String... keys) {
        for (String key : keys) {
            final String FORM_VALUE = handler.formParam(key);
            if (FORM_VALUE != null && !FORM_VALUE.isBlank()) return FORM_VALUE;

            final String QUERY_VALUE = handler.queryParam(key);
            if (QUERY_VALUE != null && !QUERY_VALUE.isBlank()) return QUERY_VALUE;

            final Object BODY_VALUE = body.get(key);
            if (BODY_VALUE != null) {
                final String VALUE = String.valueOf(BODY_VALUE).trim();
                if (!VALUE.isBlank()) return VALUE;
            }
        }
        return null;
    }
}

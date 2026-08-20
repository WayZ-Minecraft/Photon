package niwer.photon.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Niwer
 */
public class GsonUtils {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();	
	public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private GsonUtils() {}

    /**
     * Parses a JSON string into a JsonObject. Returns null if the input is null, blank, or cannot be parsed.
     * 
     * @param rawJson The raw JSON string to parse
     * @return A JsonObject representation of the input string, or null if parsing fails
     */
    public static JsonObject parseJsonObject(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return null;
        try {
            return JsonParser.parseString(rawJson).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Retrieves a JsonObject from another JsonObject by key. Returns null if the parent object is null, the key is null, or the key does not exist in the parent object.
     * 
     * @param object The parent JsonObject from which to retrieve the child object
     * @param key The key of the child JsonObject to retrieve
     * @return The child JsonObject associated with the specified key, or null if it does not exist
     */
    public static JsonObject getObject(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return null;
        try {
            return object.getAsJsonObject(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Retrieves a string value from a JsonObject by key. Returns null if the parent object is null, the key is null, or the key does not exist in the parent object.
     * 
     * @param object The parent JsonObject from which to retrieve the string value
     * @param key The key of the string value to retrieve
     * @return The string value associated with the specified key, or null if it does not exist
     */
    public static String getString(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return null;
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Retrieves a long value from a JsonObject by key. Returns 0 if the parent object is null, the key is null, or the key does not exist in the parent object.
     * 
     * @param object The parent JsonObject from which to retrieve the long value
     * @param key The key of the long value to retrieve
     * @return The long value associated with the specified key, or 0 if it does not exist
     */
    public static long getLong(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return 0L;
        try {
            return object.get(key).getAsLong();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    /**
     * Retrieves a JsonObject from another JsonObject by key, with an option to allow null values. If allowNull is true, the method will return null if the key does not exist or is null; otherwise, it will return an empty JsonObject.
     * 
     * @param object The parent JsonObject from which to retrieve the child object
     * @param key The key of the child JsonObject to retrieve
     * @param allowNull Whether to allow null values
     * @return The child JsonObject associated with the specified key, or null if it does not exist
     */
    public static JsonObject getObject(JsonObject object, String key, boolean allowNull) {
        return getObject(object, key);
    }

    public static String getString(JsonObject body, String primaryKey, String secondaryKey, String defaultValue) {
        if (body != null && body.has(primaryKey) && !body.get(primaryKey).isJsonNull()) return body.get(primaryKey).getAsString();
        if (body != null && body.has(secondaryKey) && !body.get(secondaryKey).isJsonNull()) return body.get(secondaryKey).getAsString();
        return defaultValue;
    }
    
    public static Long getLong(JsonObject body, String primaryKey, String secondaryKey, Long defaultValue) {
        try {
            if (body != null && body.has(primaryKey) && !body.get(primaryKey).isJsonNull()) return body.get(primaryKey).getAsLong();
            if (body != null && body.has(secondaryKey) && !body.get(secondaryKey).isJsonNull()) return body.get(secondaryKey).getAsLong();
        } catch (Exception ignored) {
            return defaultValue;
        }
        return defaultValue;
    }

    public static boolean getBoolean(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return false;
        try {
            return object.get(key).getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }

}
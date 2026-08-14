package niwer.photon.objects;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interface for objects that can provide a payload representation of their data.
 * 
 * @author Niwer
 */
public interface IPayloadProvider {

    /**
     * Returns a map representation of the object's data, suitable for serialization or transmission.
     * 
     * @return A map containing the object's data as key-value pairs.
     */
    default public Map<String, Object> payload() {
        final Map<String, Object> PAYLOAD = new LinkedHashMap<>();

        /* Try to get all fields */
        for(final var FIELD : this.getClass().getDeclaredFields()) {
            final String FIELD_NAME = FIELD.getName(); // Get the name of the field

            /* Try to get the value of the field */
            Object value = null;
            try {
                FIELD.setAccessible(true);
                value = FIELD.get(this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + FIELD_NAME, e);
            }

            /* Put the field name and value in the payload */
            PAYLOAD.put(FIELD_NAME, value);
        }

        return PAYLOAD;
    }
}

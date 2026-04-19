package niwer.photon.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.Test;

class ObjectLicenseTest {

    @Test
    void isExpiredReturnsTrueOnlyForPastDates() {
        final ObjectLicense expired = new ObjectLicense("key", "product", "Alice", "alice@example.com", "order-1", "ACTIVE", new Date(System.currentTimeMillis() - 1_000L));
        final ObjectLicense active = new ObjectLicense("key", "product", "Alice", "alice@example.com", "order-1", "ACTIVE", new Date(System.currentTimeMillis() + 86_400_000L));

        assertTrue(expired.isExpired());
        assertFalse(active.isExpired());
    }

    @Test
    void accessorsExposeConstructorValuesAndDefaults() throws Exception {
        final Date expiresAt = new Date(System.currentTimeMillis() + 86_400_000L);
        final ObjectLicense license = new ObjectLicense("license-1", "product-1", "Alice", "alice@example.com", "order-1", "ACTIVE", expiresAt);

        setField(license, "hwid", "hardware-1");
        setField(license, "activatedAt", new Date(123_456L));

        assertEquals("license-1", license.licenseKey());
        assertEquals("product-1", license.productId());
        assertEquals("Alice", license.customerName());
        assertEquals("alice@example.com", license.customerEmail());
        assertEquals("order-1", license.tebexOrderId());
        assertEquals("hardware-1", license.hwid());
        assertEquals("ACTIVE", license.status());
        assertNotNull(license.createdAt());
        assertNotNull(license.activatedAt());
        assertEquals(expiresAt, license.expiresAt());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        final Field field = ObjectLicense.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
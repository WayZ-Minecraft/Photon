package niwer.photon.sqlreal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.objects.ObjectLicense;
import niwer.queryon.QueryonException;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;

class LicenseTableSqlTest {

    private static final String CLASS_NAME = "niwer.photon.sql.LicenseTable";

    @AfterEach
    void resetState() {
        SelectionManager.reset();
        InsertionManager.reset();
        UpdateManager.reset();
    }

    @Test
    void normalizeKeyAndStatusParsingWorkAsExpected() throws Exception {
        final Class<?> statusClass = SqlProductionTestSupport.nestedClass(CLASS_NAME, "LicenseStatus");

        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "normalizeKey", new Class<?>[] { String.class }, new Object[] { null }));
        assertEquals("ABC-123", SqlProductionTestSupport.invokeStatic(CLASS_NAME, "normalizeKey", new Class<?>[] { String.class }, " abc-123 "));

        assertEquals(Enum.valueOf((Class<Enum>) statusClass, "ISSUED"), statusClass.getDeclaredMethod("fromString", String.class).invoke(null, (Object) null));
        assertEquals(Enum.valueOf((Class<Enum>) statusClass, "ISSUED"), statusClass.getDeclaredMethod("fromString", String.class).invoke(null, "unknown"));
        assertEquals(Enum.valueOf((Class<Enum>) statusClass, "ACTIVE"), statusClass.getDeclaredMethod("fromString", String.class).invoke(null, "active"));
    }

    @Test
    void issueLicenseStoresNormalizedKeyAndReturnsIssuedObject() throws Exception {
        final Date expiresAt = new Date(123_456L);
        final ObjectLicense issued = new ObjectLicense("ABC-123", "product", "Alice", "alice@example.com", "order-1", "ISSUED", expiresAt);
        SelectionManager.setNextSerializableResult(issued);

        final Object result = SqlProductionTestSupport.invokeStatic(
            CLASS_NAME,
            "issueLicense",
            new Class<?>[] { String.class, String.class, String.class, String.class, String.class, Date.class },
            " abc-123 ", "product", "Alice", "alice@example.com", "order-1", expiresAt
        );

        assertEquals(issued, result);
        assertEquals("ABC-123", InsertionManager.lastCall().rows().get(0)[0]);
        assertEquals("product", InsertionManager.lastCall().rows().get(0)[1]);
        assertEquals("Alice", InsertionManager.lastCall().rows().get(0)[2]);
        assertEquals("alice@example.com", InsertionManager.lastCall().rows().get(0)[3]);
        assertEquals("order-1", InsertionManager.lastCall().rows().get(0)[4]);
        assertEquals("ISSUED", InsertionManager.lastCall().rows().get(0)[5]);
        assertEquals(expiresAt, InsertionManager.lastCall().rows().get(0)[6]);
    }

    @Test
    void getByKeyAndGetByTebexOrderIdHandleInvalidInputs() throws Exception {
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getByKey", new Class<?>[] { String.class }, new Object[] { null }));
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getByTebexOrderId", new Class<?>[] { String.class }, " "));
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "exists", new Class<?>[] { String.class }, " "));
    }

    @Test
    void getByKeyGetByOrderIdAndExistsReturnConfiguredLicense() throws Exception {
        final ObjectLicense license = new ObjectLicense("ABC-123", "product", "Alice", "alice@example.com", "order-1", "ISSUED", new Date());

        SelectionManager.setNextSerializableResult(license);
        assertEquals(license, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getByKey", new Class<?>[] { String.class }, " abc-123 "));

        SelectionManager.setNextSerializableResult(license);
        assertEquals(license, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getByTebexOrderId", new Class<?>[] { String.class }, "order-1"));

        SelectionManager.setNextSerializableResult(license);
        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "exists", new Class<?>[] { String.class }, "abc-123"));
    }

    @Test
    void activateAndRevokeUpdateTheExpectedColumns() throws Exception {
        final Class<?> statusClass = SqlProductionTestSupport.nestedClass(CLASS_NAME, "LicenseStatus");

        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "activate", new Class<?>[] { String.class, String.class }, null, "hwid"));
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "revoke", new Class<?>[] { String.class }, " "));

        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "activate", new Class<?>[] { String.class, String.class }, " abc-123 ", "hwid-1"));
        assertEquals("hwid-1", UpdateManager.lastCall().values().get("hwid"));
        assertEquals(Enum.valueOf((Class<Enum>) statusClass, "ACTIVE").name(), UpdateManager.lastCall().values().get("status"));
        assertEquals(1, UpdateManager.lastCall().whereClauses().size());

        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "revoke", new Class<?>[] { String.class }, " abc-123 "));
        assertEquals(Enum.valueOf((Class<Enum>) statusClass, "REVOKED").name(), UpdateManager.lastCall().values().get("status"));
    }

    @Test
    void activateAndRevokeReturnFalseWhenUpdateFails() throws Exception {
        UpdateManager.setNextFailure(new QueryonException("boom"));
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "activate", new Class<?>[] { String.class, String.class }, "abc", "hwid"));

        UpdateManager.reset();
        UpdateManager.setNextFailure(new QueryonException("boom"));
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "revoke", new Class<?>[] { String.class }, "abc"));
    }
}
package niwer.photon.sqlreal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.queryon.queries.interaction.DeletionManager;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;

class SecurityTablesSqlTest {

    @AfterEach
    void resetState() {
        SelectionManager.reset();
        InsertionManager.reset();
        DeletionManager.reset();
    }

    @Test
    void hwidTableSaveExistLookupAndDeleteWork() throws Exception {
        final String className = "niwer.photon.sql.HWIDTable";

        SqlProductionTestSupport.invokeStatic(className, "save", new Class<?>[] { String.class, String.class, String.class }, "uuid-1", "hwid-1", "Windows");
        assertEquals("uuid-1", InsertionManager.lastCall().rows().get(0)[0]);
        assertEquals("hwid-1", InsertionManager.lastCall().rows().get(0)[1]);
        assertEquals("Windows", InsertionManager.lastCall().rows().get(0)[2]);

        SelectionManager.setNextHasResult(true);
        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(className, "exist", new Class<?>[] { String.class }, "uuid-1"));

        SelectionManager.setNextHasResult(false);
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(className, "exist", new Class<?>[] { String.class }, "uuid-2"));

        SelectionManager.setNextPrimitiveResult("hwid-1");
        assertEquals("hwid-1", SqlProductionTestSupport.invokeStatic(className, "getHWID", new Class<?>[] { String.class }, "uuid-1"));

        SqlProductionTestSupport.invokeStatic(className, "deleteHWID", new Class<?>[] { String.class, String.class }, "uuid-1", "hwid-1");
        assertEquals(1, DeletionManager.lastCall().whereClauses().size());
    }

    @Test
    void crashAndAnticheatTablesStoreTheirPayloads() throws Exception {
        final String crashClass = "niwer.photon.sql.CrashReportTable";
        final String anticheatClass = "niwer.photon.sql.AnticheatTable";

        SqlProductionTestSupport.invokeStatic(crashClass, "save", new Class<?>[] { String.class, String.class, String.class }, "uuid-1", "crash.log", "Traceback");
        assertEquals("uuid-1", InsertionManager.lastCall().rows().get(0)[0]);
        assertEquals("crash.log", InsertionManager.lastCall().rows().get(0)[1]);
        assertEquals("Traceback", InsertionManager.lastCall().rows().get(0)[2]);

        SqlProductionTestSupport.invokeStatic(anticheatClass, "save", new Class<?>[] { String.class, String.class, String.class, String.class }, "uuid-2", "ac.log", "Cheat", "Windows");
        assertEquals("uuid-2", InsertionManager.lastCall().rows().get(0)[0]);
        assertEquals("ac.log", InsertionManager.lastCall().rows().get(0)[1]);
        assertEquals("Cheat", InsertionManager.lastCall().rows().get(0)[2]);
        assertEquals("Windows", InsertionManager.lastCall().rows().get(0)[3]);
    }
}
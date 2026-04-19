package niwer.photon.sqlreal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.objects.ObjectServer;
import niwer.queryon.QueryonException;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;

class ServerTableSqlTest {

    private static final String CLASS_NAME = "niwer.photon.sql.ServerTable";

    @AfterEach
    void resetState() {
        SelectionManager.reset();
        InsertionManager.reset();
        UpdateManager.reset();
    }

    @Test
    void saveOrUpdateIgnoresInvalidServers() throws Exception {
        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "saveOrUpdate", new Class<?>[] { ObjectServer.class }, new Object[] { null });

        final ObjectServer noIp = new ObjectServer();
        noIp.serverIP = " ";
        noIp.serverPort = 25565;
        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "saveOrUpdate", new Class<?>[] { ObjectServer.class }, noIp);
        assertNull(InsertionManager.lastCall());
        assertNull(UpdateManager.lastCall());
    }

    @Test
    void saveOrUpdateUsesInsertAndUpdateBranches() throws Exception {
        final ObjectServer server = new ObjectServer();
        server.serverIP = "127.0.0.1";
        server.serverPort = 25565;
        server.serverName = "Photon";
        server.serverMOTD = "MOTD";
        server.queuePort = 25566;
        server.last_seen_at = new Date();

        SelectionManager.setNextHasResult(false);
        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "saveOrUpdate", new Class<?>[] { ObjectServer.class }, server);
        assertEquals("Photon", InsertionManager.lastCall().rows().get(0)[0]);

        SelectionManager.setNextHasResult(true);
        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "saveOrUpdate", new Class<?>[] { ObjectServer.class }, server);
        assertEquals("server_name", UpdateManager.lastCall().values().keySet().iterator().next());
    }

    @Test
    void getVisibleServersFiltersOutExpiredEntriesAndHandlesFailures() throws Exception {
        final ObjectServer fresh = new ObjectServer();
        fresh.last_seen_at = new Date(System.currentTimeMillis());
        final ObjectServer stale = new ObjectServer();
        stale.last_seen_at = new Date(System.currentTimeMillis() - (ServerTableSqlTestHelper.TTL_MULTIPLIER));
        final ObjectServer missing = new ObjectServer();

        SelectionManager.setNextListResult(List.of(fresh, stale, missing));
        assertEquals(List.of(fresh), SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getVisibleServers", new Class<?>[0]));

        SelectionManager.setNextFailure(new QueryonException("boom"));
        assertTrue(((List<?>) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getVisibleServers", new Class<?>[0])).isEmpty());
    }

    @Test
    void getServerRejectsInvalidInputAndUsesSelectionResult() throws Exception {
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getServer", new Class<?>[] { String.class, int.class }, null, 25565));
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getServer", new Class<?>[] { String.class, int.class }, "127.0.0.1", 0));

        final ObjectServer server = new ObjectServer();
        SelectionManager.setNextSerializableResult(server);
        assertEquals(server, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getServer", new Class<?>[] { String.class, int.class }, "127.0.0.1", 25565));

        SelectionManager.setNextFailure(new QueryonException("boom"));
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getServer", new Class<?>[] { String.class, int.class }, "127.0.0.1", 25565));
    }

    private static final class ServerTableSqlTestHelper {
        private static final long TTL_MULTIPLIER = 30L * 24L * 60L * 60L * 1000L + 1000L;
    }
}
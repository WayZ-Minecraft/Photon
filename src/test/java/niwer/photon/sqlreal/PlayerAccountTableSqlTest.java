package niwer.photon.sqlreal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.objects.ObjectPlayerAccount;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.queries.interaction.DeletionManager;

class PlayerAccountTableSqlTest {

    private static final String CLASS_NAME = "niwer.photon.sql.PlayerAccountTable";

    @AfterEach
    void resetState() {
        SelectionManager.reset();
        InsertionManager.reset();
        UpdateManager.reset();
        DeletionManager.reset();
    }

    @Test
    void createAccountRejectsInvalidInputsAndDuplicates() throws Exception {
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "createAccount", new Class<?>[] { String.class, String.class, String.class }, null, "a@example.com", "pw"));
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "createAccount", new Class<?>[] { String.class, String.class, String.class }, "user", " ", "pw"));

        SelectionManager.setNextHasResult(true);
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "createAccount", new Class<?>[] { String.class, String.class, String.class }, "user", "a@example.com", "pw"));

        SelectionManager.setNextHasResultSequence(false, true);
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "createAccount", new Class<?>[] { String.class, String.class, String.class }, "taken", "a@example.com", "pw"));
    }

    @Test
    void createAccountStoresNormalizedValues() throws Exception {
        SelectionManager.setNextHasResult(false);
        SelectionManager.setNextSerializableResult(null);

        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "createAccount", new Class<?>[] { String.class, String.class, String.class }, "  Alice  ", "  Alice@Example.com  ", "pw");

        final Object[] row = InsertionManager.lastCall().rows().get(0);
        assertEquals("Alice", row[1]);
        assertEquals("alice@example.com", row[2]);
        assertEquals("pw", row[3]);
        assertEquals("[]", row[5]);
    }

    @Test
    void lookupAndValidationHelpersHandleNullAndConfiguredResults() throws Exception {
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAccountByUUID", new Class<?>[] { String.class }, (Object) null));
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAccountByEmail", new Class<?>[] { String.class }, " "));
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAccountByUsername", new Class<?>[] { String.class }, (Object) null));
        assertNull(SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAccountByDiscordID", new Class<?>[] { String.class }, ""));
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "emailExists", new Class<?>[] { String.class }, (Object) null));
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "usernameExists", new Class<?>[] { String.class }, " "));
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "isAuthCodeValid", new Class<?>[] { String.class, String.class }, (Object) null, "code"));

        SelectionManager.setNextSerializableResult(new ObjectPlayerAccount());
        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "existByUUID", new Class<?>[] { String.class }, "uuid-1"));

        SelectionManager.setNextHasResult(true);
        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "emailExists", new Class<?>[] { String.class }, "alice@example.com"));

        SelectionManager.setNextHasResult(true);
        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "usernameExists", new Class<?>[] { String.class }, "alice"));

        SelectionManager.setNextHasResult(true);
        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "isAuthCodeValid", new Class<?>[] { String.class, String.class }, "uuid-1", "code-1"));

        SelectionManager.setNextPrimitiveResult("token-1");
        assertEquals("token-1", SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getTokenByEmail", new Class<?>[] { String.class }, "alice@example.com"));
    }

    @Test
    void lookupHelpersReturnConfiguredAccounts() throws Exception {
        final ObjectPlayerAccount account = new ObjectPlayerAccount();

        SelectionManager.setNextSerializableResult(account);
        assertEquals(account, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAccountByUUID", new Class<?>[] { String.class }, "uuid-1"));

        SelectionManager.setNextSerializableResult(account);
        assertEquals(account, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAccountByEmail", new Class<?>[] { String.class }, "alice@example.com"));

        SelectionManager.setNextSerializableResult(account);
        assertEquals(account, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAccountByUsername", new Class<?>[] { String.class }, "alice"));

        SelectionManager.setNextSerializableResult(account);
        assertEquals(account, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAccountByDiscordID", new Class<?>[] { String.class }, "discord-1"));
    }

    @Test
    void updateAndDeleteHelpersUseTheExpectedColumns() throws Exception {
        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "updateDiscordID", new Class<?>[] { String.class, String.class }, null, "discord");
        assertNull(UpdateManager.lastCall());

        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "updateDiscordID", new Class<?>[] { String.class, String.class }, "uuid-1", "discord-1");
        assertEquals("discord-1", UpdateManager.lastCall().values().get("discordID"));

        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "setServerCreator", new Class<?>[] { String.class, boolean.class }, "uuid-1", true);
        assertEquals(true, UpdateManager.lastCall().values().get("serverCreator"));

        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "deleteAccount", new Class<?>[] { String.class }, "uuid-1");
        assertEquals(1, DeletionManager.lastCall().whereClauses().size());
    }

    @Test
    void getAllAccountsReturnsTheConfiguredList() throws Exception {
        final ObjectPlayerAccount account = new ObjectPlayerAccount();
        SelectionManager.setNextListResult(List.of(account));

        final Object result = SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAllAccounts", new Class<?>[0]);
        assertEquals(List.of(account), result);
    }
}
package niwer.photon.sqlreal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.util.TranslationManager.Language;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;

class DiscordProfileTableSqlTest {

    private static final String CLASS_NAME = "niwer.photon.sql.DiscordProfileTable";

    @AfterEach
    void resetState() {
        SelectionManager.reset();
        InsertionManager.reset();
        UpdateManager.reset();
    }

    @Test
    void createProfileUsesInsertOrIgnore() throws Exception {
        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "createProfile", new Class<?>[] { String.class }, "discord-1");

        assertEquals(true, InsertionManager.lastCall().insertOrIgnore());
        assertEquals("discord-1", InsertionManager.lastCall().rows().get(0)[0]);
    }

    @Test
    void getLanguageDefaultsAndReturnsConfiguredValue() throws Exception {
        SelectionManager.setNextPrimitiveResult(null);
        assertEquals(Language.ENGLISH, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getLanguage", new Class<?>[] { String.class }, "discord-1"));

        SelectionManager.setNextPrimitiveResult("FRENCH");
        assertEquals(Language.FRENCH, SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getLanguage", new Class<?>[] { String.class }, "discord-1"));
    }

    @Test
    void setLanguageAndFirstConnectionUpdateTheProfile() throws Exception {
        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "setLanguage", new Class<?>[] { String.class, Language.class }, "discord-1", Language.GERMAN);
        assertEquals("GERMAN", UpdateManager.lastCall().values().get("language"));

        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "setFirstConnection", new Class<?>[] { String.class, boolean.class }, "discord-1", false);
        assertEquals(false, UpdateManager.lastCall().values().get("first_connection"));
    }

    @Test
    void firstConnectionReflectsSelectionResult() throws Exception {
        SelectionManager.setNextHasResult(true);
        assertTrue((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "isFirstConnection", new Class<?>[] { String.class }, "discord-1"));

        SelectionManager.setNextHasResult(false);
        assertFalse((Boolean) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "isFirstConnection", new Class<?>[] { String.class }, "discord-1"));
    }
}
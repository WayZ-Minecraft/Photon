package niwer.photon.sqlreal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.dv8tion.jda.api.audit.ActionType;
import niwer.photon.objects.ObjectDiscordLog;
import niwer.queryon.queries.interaction.DeletionManager;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;

class DiscordLogTableSqlTest {

    private static final String CLASS_NAME = "niwer.photon.sql.DiscordLogTable";
    private static final String MODERATION_TYPE_NAME = CLASS_NAME + "$ModerationType";

    @AfterEach
    void resetState() {
        SelectionManager.reset();
        InsertionManager.reset();
        DeletionManager.reset();
    }

    @Test
    void moderationTypesMapToDiscordActionTypes() throws Exception {
        final Class<?> moderationType = SqlProductionTestSupport.load(MODERATION_TYPE_NAME);

        assertEquals(ActionType.BAN, moderationType.getEnumConstants()[0].getClass().getMethod("toDiscordActionType").invoke(moderationType.getEnumConstants()[0]));
        assertEquals(ActionType.UNBAN, moderationType.getEnumConstants()[1].getClass().getMethod("toDiscordActionType").invoke(moderationType.getEnumConstants()[1]));
        assertEquals(ActionType.KICK, moderationType.getEnumConstants()[2].getClass().getMethod("toDiscordActionType").invoke(moderationType.getEnumConstants()[2]));
        assertEquals(ActionType.MEMBER_UPDATE, moderationType.getEnumConstants()[3].getClass().getMethod("toDiscordActionType").invoke(moderationType.getEnumConstants()[3]));
    }

    @Test
    void saveStoresTheExpectedFields() throws Exception {
        final Class<?> moderationType = SqlProductionTestSupport.load(MODERATION_TYPE_NAME);
        final Object ban = Enum.valueOf((Class<Enum>) moderationType, "BAN");

        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "save", new Class<?>[] { String.class, String.class, moderationType, String.class, String.class, long.class }, "guild-1", "user-1", ban, "reason", "mod-1", 42L);

        final Object[] row = InsertionManager.lastCall().rows().get(0);
        assertEquals("guild-1", row[0]);
        assertEquals("user-1", row[1]);
        assertEquals("BAN", row[2]);
        assertEquals("reason", row[3]);
        assertEquals("mod-1", row[4]);
        assertEquals(42L, row[5]);
    }

    @Test
    void listQueriesUseExpectedOrderingAndFilters() throws Exception {
        final Object log = new ObjectDiscordLog();
        SelectionManager.setNextListResult(List.of(log));

        assertEquals(List.of(log), SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getByDiscordUserID", new Class<?>[] { String.class }, "user-1"));
        assertEquals("discord_user_id = user-1", SelectionManager.lastCall().whereClauses().get(0));

        SelectionManager.setNextListResult(List.of(log));
        assertEquals(List.of(log), SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getByGuild", new Class<?>[] { String.class }, "guild-1"));
        assertEquals(SelectionManager.EnumOrder.DESC, SelectionManager.lastCall().order());

        final Class<?> moderationType = SqlProductionTestSupport.load(MODERATION_TYPE_NAME);
        final Object ban = Enum.valueOf((Class<Enum>) moderationType, "BAN");
        SelectionManager.setNextListResult(List.of(log));
        assertEquals(List.of(log), SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getByGuildAndType", new Class<?>[] { String.class, moderationType }, "guild-1", ban));
        assertEquals(SelectionManager.EnumOrder.DESC, SelectionManager.lastCall().order());
    }

    @Test
    void deleteByDiscordIdUsesDeletionManager() throws Exception {
        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "deleteByDiscordID", new Class<?>[] { String.class }, "user-1");

        assertEquals(1, DeletionManager.lastCall().whereClauses().size());
    }
}
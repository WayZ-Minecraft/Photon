package niwer.photon.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.Test;

import niwer.queryon.tables.api.IColumnField;

class ObjectDiscordLogTest {

    @Test
    void defaultFieldValuesAreInitialized() throws Exception {
        final ObjectDiscordLog log = new ObjectDiscordLog();

        assertEquals(0, readInt(log, "id"));
        assertEquals(0L, readLong(log, "durationSeconds"));
        assertNotNull(readObject(log, "timestamp"));
    }

    @Test
    void fieldMappingsRemainAnnotated() throws Exception {
        assertEquals("discord_user_id", annotationName("id"));
        assertEquals("guild_id", annotationName("guildID"));
        assertEquals("discord_user_id", annotationName("discordID"));
        assertEquals("type", annotationName("type"));
        assertEquals("reason", annotationName("reason"));
        assertEquals("moderator_discord_id", annotationName("moderatorDiscordID"));
        assertEquals("duration_seconds", annotationName("durationSeconds"));
        assertEquals("timestamp", annotationName("timestamp"));
    }

    @Test
    void defaultTimestampIsInitialized() throws Exception {
        final ObjectDiscordLog log = new ObjectDiscordLog();

        assertTrue(readObject(log, "timestamp") instanceof Date);
    }

    private static String annotationName(String fieldName) throws Exception {
        final Field field = ObjectDiscordLog.class.getDeclaredField(fieldName);
        final IColumnField annotation = field.getAnnotation(IColumnField.class);
        return annotation == null ? null : annotation.name();
    }

    private static int readInt(Object target, String fieldName) throws Exception {
        final Field field = ObjectDiscordLog.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(target);
    }

    private static long readLong(Object target, String fieldName) throws Exception {
        final Field field = ObjectDiscordLog.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getLong(target);
    }

    private static Object readObject(Object target, String fieldName) throws Exception {
        final Field field = ObjectDiscordLog.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}

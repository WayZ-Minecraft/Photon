package niwer.photon.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import niwer.queryon.tables.api.IColumnField;

class ObjectServerTest {

    @Test
    void hashCodeDependsOnIpAndPort() {
        final ObjectServer first = new ObjectServer();
        first.serverIP = "192.0.2.10";
        first.serverPort = 25565;

        final ObjectServer second = new ObjectServer();
        second.serverIP = "192.0.2.10";
        second.serverPort = 25565;

        final ObjectServer different = new ObjectServer();
        different.serverIP = "192.0.2.11";
        different.serverPort = 25565;

        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first.hashCode(), different.hashCode());
        assertEquals(0, new ObjectServer().hashCode());
        assertNull(new ObjectServer().serverIP);
    }

    @Test
    void fieldsRemainAnnotatedForPersistence() throws Exception {
        assertEquals("server_name", annotationName("serverName"));
        assertEquals("server_motd", annotationName("serverMOTD"));
        assertEquals("server_ip", annotationName("serverIP"));
        assertEquals("server_port", annotationName("serverPort"));
        assertEquals("queue_port", annotationName("queuePort"));
        assertEquals("last_seen_at", annotationName("last_seen_at"));
        assertEquals("site_url", annotationName("site"));
        assertEquals("discord", annotationName("discord"));
    }

    private static String annotationName(String fieldName) throws Exception {
        final Field field = ObjectServer.class.getDeclaredField(fieldName);
        final IColumnField annotation = field.getAnnotation(IColumnField.class);
        return annotation == null ? null : annotation.name();
    }
}
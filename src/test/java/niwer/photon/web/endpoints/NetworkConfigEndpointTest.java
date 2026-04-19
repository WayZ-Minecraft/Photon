package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.Directories;
import niwer.photon.PhotonEngine;

class NetworkConfigEndpointTest {

    @AfterEach
    void resetCurrentIp() throws Exception {
        setCurrentIp(null);
        Directories.config = Directories.getConfig();
    }

    @Test
    void exposesTheExpectedPathAndMethod() {
        final NetworkConfigEndpoint endpoint = new NetworkConfigEndpoint();

        assertEquals("/api/network-config", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.GET, endpoint.method());
    }

    @Test
    void handleReturnsTheExpectedConfigPayload() throws Exception {
        final Directories.NetworkConfig config = new Directories.NetworkConfig();
        config.discord_bot_id = "bot-123";
        config.mod_version = "2.4.6";
        config.webserver_port = 8123;
        config.twitter_url = "https://example.com/twitter";
        config.twitch_url = "https://example.com/twitch";
        config.youtube_url = "https://example.com/youtube";
        config.discord_url = "https://example.com/discord";
        config.website_url = "https://example.com";
        Directories.config = config;
        setCurrentIp("203.0.113.10");

        final ContextStubTest stub = new ContextStubTest();
        new NetworkConfigEndpoint().handle(stub.context());

        assertNotNull(stub.jsonBody());
        assertNull(stub.statusCode());
        assertEquals("bot-123", invokeRecordGetter(stub.jsonBody(), "discord_bot_id"));
        assertEquals("2.4.6", invokeRecordGetter(stub.jsonBody(), "mod_version"));
        assertEquals("203.0.113.10", invokeRecordGetter(stub.jsonBody(), "network_ip"));
        assertEquals(8123, invokeRecordGetter(stub.jsonBody(), "webserver_port"));
        assertEquals("https://example.com", invokeRecordGetter(stub.jsonBody(), "website_url"));
    }

    private static Object invokeRecordGetter(Object target, String methodName) throws Exception {
        final Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private static void setCurrentIp(String value) throws Exception {
        final Field field = PhotonEngine.class.getDeclaredField("currentIP");
        field.setAccessible(true);
        field.set(null, value);
    }
}

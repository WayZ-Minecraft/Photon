package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.Directories;
import niwer.photon.util.os.OperatingSystem;
import niwer.photon.util.updater.UpdateChannel;
import niwer.photon.util.updater.UpdateFileType;

class UpdateEndpointTest {

    @AfterEach
    void resetConfig() {
        Directories.config = Directories.getConfig();
    }

    @Test
    void exposesTheExpectedPathAndMethod() {
        final UpdateEndpoint endpoint = new UpdateEndpoint();

        assertEquals("/api/update", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.GET, endpoint.method());
    }

    @Test
    void rejectsMissingParameters() {
        final ContextStub stub = new ContextStub();

        new UpdateEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Missing parameters", stub.resultBody());
    }

    @Test
    void rejectsInvalidTypeOrChannel() {
        final ContextStub stub = new ContextStub()
            .queryParam("type", "plugin")
            .queryParam("channel", "dev");

        new UpdateEndpoint().handle(stub.context());

        assertEquals(400, stub.statusCode());
        assertEquals("Invalid update type or channel", stub.resultBody());
    }

    @Test
    void returns404WhenUpdateFileDoesNotExist() {
        Directories.config = new Directories.NetworkConfig();
        Directories.config.filePaths = Map.of(
            UpdateFileType.NETWORK, Map.of(UpdateChannel.DEV, "c:/nope/network-dev.jar")
        );

        final ContextStub stub = new ContextStub()
            .queryParam("type", "network")
            .queryParam("channel", "dev");

        new UpdateEndpoint().handle(stub.context());

        assertEquals(404, stub.statusCode());
        assertEquals("Update not found", stub.resultBody());
    }

    @Test
    void metadataOnlyReturnsSha1AndSize() throws Exception {
        final Path file = Files.createTempFile("photon-update", ".jar");
        Files.writeString(file, "Photon update", StandardCharsets.UTF_8);

        Directories.config = new Directories.NetworkConfig();
        Directories.config.filePaths = Map.of(
            UpdateFileType.NETWORK, Map.of(UpdateChannel.DEV, file.toString())
        );

        final ContextStub stub = new ContextStub()
            .queryParam("type", "network")
            .queryParam("channel", "dev")
            .queryParam("metadata", "true");

        new UpdateEndpoint().handle(stub.context());

        assertNotNull(stub.jsonBody());
        assertEquals(OperatingSystem.hash(file.toFile(), "SHA-1"), invokeRecordGetter(stub.jsonBody(), "sha1"));
        assertEquals((int) Files.size(file), invokeRecordGetter(stub.jsonBody(), "size"));

        Files.deleteIfExists(file);
    }

    @Test
    void fullDownloadReturnsBinaryPayloadAndSha1Header() throws Exception {
        final Path file = Files.createTempFile("photon-update", ".jar");
        final byte[] bytes = "Photon update".getBytes(StandardCharsets.UTF_8);
        Files.write(file, bytes);

        Directories.config = new Directories.NetworkConfig();
        Directories.config.filePaths = Map.of(
            UpdateFileType.NETWORK, Map.of(UpdateChannel.DEV, file.toString())
        );

        final ContextStub stub = new ContextStub()
            .queryParam("type", "network")
            .queryParam("channel", "dev");

        new UpdateEndpoint().handle(stub.context());

        assertEquals("application/octet-stream", stub.contentType());
        assertEquals(OperatingSystem.hash(file.toFile(), "SHA-1"), stub.responseHeaders().get("Update-Sha1"));
        assertArrayEquals(bytes, (byte[]) stub.resultBody());

        Files.deleteIfExists(file);
    }

    @Test
    void returns500WhenTheUpdateFileCannotBeRead() throws Exception {
        final Path directory = Files.createTempDirectory("photon-update-dir");

        Directories.config = new Directories.NetworkConfig();
        Directories.config.filePaths = Map.of(
            UpdateFileType.NETWORK, Map.of(UpdateChannel.DEV, directory.toString())
        );

        final ContextStub stub = new ContextStub()
            .queryParam("type", "network")
            .queryParam("channel", "dev");

        new UpdateEndpoint().handle(stub.context());

        assertEquals(500, stub.statusCode());
        assertEquals("Unable to read update file", stub.resultBody());

        Files.deleteIfExists(directory);
    }

    private static Object invokeRecordGetter(Object target, String methodName) throws Exception {
        final Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }
}
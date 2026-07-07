package niwer.photon.web.endpoints.contentpacks;

import java.nio.file.Files;
import java.nio.file.Path;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.sql.PackProductTable;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class ContentPackDeleteEndpoint implements IEndpoint {

	@Override public String path() { return "/api/content-packs/remove/{packId}"; }

	@Override public HttpMethod method() { return HttpMethod.DELETE; }

	@Override
	public void handle(Context handler) {
		if (AdminSessionManager.requireAdministrator(handler) == null) return;
		if (!AdminSessionManager.validateCsrf(handler)) { handler.status(403).result("Invalid CSRF token"); return; }

		final String packId = handler.pathParam("packId");
		if (packId == null || packId.isBlank()) {
			handler.status(400).result("Missing packId");
			return;
		}

		final boolean deleted = PackProductTable.remove(packId);
		try {
			final Path filePath = Path.of(Directories.BASE_DIR.getPath(), "downloads", packId + ".zip");
			Files.deleteIfExists(filePath);
		} catch (Exception ignored) {}

		if (!deleted) {
			handler.status(404).result("Pack not found");
			return;
		}

		handler.result("Deleted");
	}
}
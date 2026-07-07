package niwer.photon.web.endpoints.contentpacks;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectPackOwnership;
import niwer.photon.objects.ObjectPackProduct;
import niwer.photon.sql.PackOwnershipTable;
import niwer.photon.sql.PackProductTable;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class ContentPackDownloadEndpoint implements IEndpoint {

	private static final Map<String, Bucket> BUCKETS = new ConcurrentHashMap<>();

	@Override public String path() { return "/downloads/{packId}"; }

	@Override public HttpMethod method() { return HttpMethod.GET; }

	@Override
	public void handle(Context handler) {
		final var account = UserSessionManager.requireAccount(handler);
		if (account == null) return;

		final String packId = handler.pathParam("packId");
		final ObjectPackProduct pack = PackProductTable.getById(packId);
		if (pack == null) {
			handler.status(404).result("Pack not found");
			return;
		}

		if (!rateLimitAllowed(account.getEmail(), packId)) {
			handler.status(429).result("Too many download attempts. Try again later.");
			return;
		}

		if (!PackOwnershipTable.owns(account.getEmail(), account.getUuid(), packId)) {
			handler.status(403).result("Forbidden");
			return;
		}

		final Path filePath = Path.of(Directories.BASE_DIR.getPath(), "downloads", packId + ".zip");
		final File file = filePath.toFile();
		if (!file.exists()) {
			handler.status(404).result("File not found");
			return;
		}

		final ObjectPackOwnership ownership = PackOwnershipTable.getByEmailAndPackId(account.getEmail(), packId);
		if (ownership != null && ownership.firstDownloadAt() == null) {
			PackOwnershipTable.markFirstDownload(account.getEmail(), account.getUuid(), packId);
		}

		handler.contentType("application/zip");
		handler.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
		try {
			handler.result(new FileInputStream(file));
		} catch (Exception e) {
			handler.status(500).result("Error serving file");
		}
	}

	private static boolean rateLimitAllowed(String email, String packId) {
		final long window = 60L * 60L * 1000L;
		final long now = System.currentTimeMillis();
		final String key = (email == null ? "" : email.toLowerCase()) + "::" + packId;
		final Bucket bucket = BUCKETS.compute(key, (k, current) -> {
			if (current == null || now - current.windowStart > window) return new Bucket(now, 1);
			return new Bucket(current.windowStart, current.attempts + 1);
		});
		return bucket.attempts <= 3;
	}

	private record Bucket(long windowStart, int attempts) {}
}
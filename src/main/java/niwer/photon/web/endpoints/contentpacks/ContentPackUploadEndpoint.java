package niwer.photon.web.endpoints.contentpacks;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.zip.ZipInputStream;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPackProduct;
import niwer.photon.sql.PackProductTable;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class ContentPackUploadEndpoint implements IEndpoint {

	@Override public String path() { return "/api/content-packs/upload"; }

	@Override public HttpMethod method() { return HttpMethod.POST; }

	@Override
	public void handle(Context handler) {
		if (AdminSessionManager.requireAdministrator(handler) == null) return;
		if (!AdminSessionManager.validateCsrf(handler)) { handler.status(403).result("Invalid CSRF token"); return; }

		final UploadedFile uploadedFile = handler.uploadedFile("file");
		if (uploadedFile == null) {
			handler.status(400).result("Missing file");
			return;
		}

		final String packId = firstNonBlank(handler.formParam("pack_id"), handler.formParam("id"));
		final String name = handler.formParam("name");
		final String stripePriceId = firstNonBlank(handler.formParam("stripe_price_id"), handler.formParam("price_id"));
		if (packId == null || packId.isBlank() || name == null || name.isBlank() || stripePriceId == null || stripePriceId.isBlank()) {
			handler.status(400).result("Missing pack_id, name, or stripe_price_id");
			return;
		}

		final String versionNumber = firstNonBlank(handler.formParam("version_number"), handler.formParam("version"));
		final String category = handler.formParam("category");
		final String description = handler.formParam("description");
		final String status = firstNonBlank(handler.formParam("status"), "ACTIVE");

		final String fileName = uploadedFile.filename();
		if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
			handler.status(400).result("Invalid file type. Only ZIP files are allowed");
			return;
		}

		final Path downloadDir = Path.of(Directories.BASE_DIR.getPath(), "downloads");
		final Path outputPath = downloadDir.resolve(PackProductTable.normalizeId(packId) + ".zip");

		try {
			if (outputPath.getParent() != null) Files.createDirectories(outputPath.getParent());
			try (InputStream input = uploadedFile.content(); ZipInputStream zip = new ZipInputStream(input)) {
				if (zip.getNextEntry() == null) {
					handler.status(400).result("Uploaded file is not a valid ZIP archive");
					return;
				}
			}

			try (InputStream input = uploadedFile.content()) {
				Files.copy(input, outputPath, StandardCopyOption.REPLACE_EXISTING);
			}
			try {
				final Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-r--r--");
				Files.setPosixFilePermissions(outputPath, permissions);
			} catch (UnsupportedOperationException ignored) {}

			final ObjectPackProduct pack = PackProductTable.upsertPack(
				packId,
				name,
				description,
				category,
				stripePriceId,
				"/downloads/" + PackProductTable.normalizeId(packId) + ".zip",
				versionNumber,
				status
			);

			handler.json(pack);
		} catch (Exception e) {
            Console.log(e).type(PhotonLogTypes.NETWORK).container(PhotonEngine.LOGGER).send();
			handler.status(500).result("Failed to upload content pack");
		}
	}

	private static String firstNonBlank(String first, String second) {
		if (first != null && !first.isBlank()) return first;
		if (second != null && !second.isBlank()) return second;
		return null;
	}
}
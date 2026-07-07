package niwer.photon.web.endpoints.contentpacks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectPackOwnership;
import niwer.photon.objects.ObjectPackProduct;
import niwer.photon.sql.PackOwnershipTable;
import niwer.photon.sql.PackProductTable;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class ContentPackOwnedEndpoint implements IEndpoint {

	@Override public String path() { return "/api/content-packs/owned"; }

	@Override public HttpMethod method() { return HttpMethod.GET; }

	@Override
	public void handle(Context handler) {
		final var account = UserSessionManager.requireAccount(handler);
		if (account == null) return;

		final List<ObjectPackOwnership> ownerships = PackOwnershipTable.getByEmail(account.getEmail());
		final List<Map<String, Object>> response = new ArrayList<>();
		for (final ObjectPackOwnership ownership : ownerships) {
			final ObjectPackProduct pack = PackProductTable.getById(ownership.packId());
			if (pack == null) continue;
			final Map<String, Object> row = new LinkedHashMap<>();
			row.put("packId", ownership.packId());
			row.put("name", pack.name());
			row.put("description", pack.description());
			row.put("category", pack.category());
			row.put("versionNumber", pack.versionNumber());
			row.put("ownedAt", ownership.purchasedAt());
			row.put("claimedSuccessfully", ownership.claimedSuccessfully());
			row.put("firstDownloadAt", ownership.firstDownloadAt());
			row.put("filePath", pack.filePath());
			response.add(row);
		}

		handler.json(response);
	}
}
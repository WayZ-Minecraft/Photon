package niwer.photon.web.endpoints.contentpacks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectPackProduct;
import niwer.photon.sql.PackOwnershipTable;
import niwer.photon.sql.PackProductTable;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class ContentPackCatalogEndpoint implements IEndpoint {

	@Override public String path() { return "/api/content-packs/catalog"; }

	@Override public HttpMethod method() { return HttpMethod.GET; }

	@Override
	public void handle(Context handler) {
		final var account = UserSessionManager.accountFromRequest(handler);
		final List<ObjectPackProduct> packs = PackProductTable.getAllActive();
		final List<Map<String, Object>> response = new ArrayList<>();

		for (final ObjectPackProduct pack : packs) {
			final Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", pack.id());
			row.put("name", pack.name());
			row.put("description", pack.description());
			row.put("category", pack.category());
			row.put("versionNumber", pack.versionNumber());
			row.put("stripePriceId", pack.stripePriceId());
			row.put("stripePaymentLink", pack.stripePaymentLink());
			row.put("filePath", pack.filePath());
			row.put("status", pack.status());
			row.put("owned", account != null && PackOwnershipTable.owns(account.getEmail(), account.getUuid(), pack.id()));
			response.add(row);
		}

		handler.json(response);
	}
}
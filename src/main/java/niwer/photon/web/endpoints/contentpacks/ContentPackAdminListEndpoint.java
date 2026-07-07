package niwer.photon.web.endpoints.contentpacks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.javalin.http.Context;
import niwer.photon.objects.ObjectPackProduct;
import niwer.photon.sql.PackProductTable;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class ContentPackAdminListEndpoint implements IEndpoint {

	@Override public String path() { return "/admin/content-packs/list"; }

	@Override public HttpMethod method() { return HttpMethod.GET; }

	@Override
	public void handle(Context handler) {
		if (AdminSessionManager.requireAdministrator(handler) == null) return;

		final List<ObjectPackProduct> packs = PackProductTable.getAllActive();
		final List<Map<String, Object>> response = new ArrayList<>();
		for (final ObjectPackProduct pack : packs) {
			final Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", pack.id());
			row.put("name", pack.name());
			row.put("versionNumber", pack.versionNumber());
			row.put("category", pack.category());
			row.put("status", pack.status());
			row.put("description", pack.description());
			row.put("stripePriceId", pack.stripePriceId());
			row.put("stripePaymentLink", pack.stripePaymentLink());
			response.add(row);
		}
		handler.json(response);
	}
}
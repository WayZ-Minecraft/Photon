package niwer.photon.web.endpoints.contentpacks;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectPackProduct;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PackProductTable;
import niwer.photon.sql.PurchaseTokenTable;
import niwer.photon.web.UserSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class ContentPackCheckoutEndpoint implements IEndpoint {

	private static final HttpClient HTTP = HttpClient.newHttpClient();

	@Override public String path() { return "/checkout/create-session/{contentType}"; }

	@Override public HttpMethod method() { return HttpMethod.POST; }

	@Override
	public void handle(Context handler) {
		final ObjectPlayerAccount account = UserSessionManager.requireAccount(handler);
		if (account == null) return;

		final String contentType = handler.pathParam("contentType");
		final String normalizedType = contentType == null ? "" : contentType.trim().toLowerCase();
		final String apiKey = Directories.getConfig().stripe_api_key;
		if (apiKey == null || apiKey.isBlank()) {
			handler.status(500).result("stripe_api_key is not configured");
			return;
		}

		final String priceId = firstNonBlank(handler.formParam("priceId"), handler.formParam("price_id"), handler.queryParam("priceId"), handler.queryParam("price_id"));
		if (priceId == null || priceId.isBlank()) {
			handler.status(400).result("Missing priceId");
			return;
		}

		final String email = account.getEmail();
		final String name = account.getUsername();
		final String purchaseToken = PurchaseTokenTable.generateToken();
		final ObjectPackProduct pack = PackProductTable.getByStripePriceId(priceId);
		final boolean packPurchase = "pack".equals(normalizedType) || "one-time-pack".equals(normalizedType);
		final Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("client_reference_id", purchaseToken);
		parameters.put("customer_email", email);
		parameters.put("mode", packPurchase ? "payment" : "subscription");
		parameters.put("success_url", buildSuccessUrl());
		parameters.put("cancel_url", buildCancelUrl());
		parameters.put("line_items[0][price]", priceId);
		parameters.put("line_items[0][quantity]", "1");
		parameters.put("metadata[type]", packPurchase ? "one-time-pack" : "subscription");
		parameters.put("metadata[customer_email]", email);
		parameters.put("metadata[customer_name]", name);
		parameters.put("metadata[purchase_token]", purchaseToken);
		if (pack != null) {
			parameters.put("metadata[content_pack_metadata_id]", pack.id());
			parameters.put("metadata[pack_id]", pack.id());
		}

		if (packPurchase && pack == null) {
			handler.status(404).result("Unknown content pack");
			return;
		}

		PurchaseTokenTable.ensurePendingPurchase(purchaseToken, purchaseToken, priceId, email, name);
		final JsonObject session = createCheckoutSession(apiKey, parameters);
		if (session == null) {
			handler.status(502).result("Unable to create checkout session");
			return;
		}

		final Map<String, Object> response = new LinkedHashMap<>();
		response.put("checkoutSessionId", getString(session, "id"));
		response.put("url", getString(session, "url"));
		response.put("clientReferenceId", purchaseToken);
		response.put("contentType", packPurchase ? "one-time-pack" : "subscription");
		response.put("priceId", priceId);
		response.put("packId", pack == null ? null : pack.id());
		handler.json(response);
	}

	private static String buildSuccessUrl() {
		return baseUrl() + "/purchase.html?token={CHECKOUT_SESSION_ID}";
	}

	private static String buildCancelUrl() {
		return baseUrl() + "/?canceled=1";
	}

	private static String baseUrl() {
		return Directories.getConfig().website_url == null || Directories.getConfig().website_url.isBlank() ? "http://localhost:" + Directories.getConfig().webserver_port : Directories.getConfig().website_url.trim();
	}

	private static JsonObject createCheckoutSession(String apiKey, Map<String, String> parameters) {
		if (apiKey == null || apiKey.isBlank() || parameters == null || parameters.isEmpty()) return null;

		try {
			final StringJoiner body = new StringJoiner("&");
			parameters.forEach((key, value) -> {
				if (key != null && value != null) body.add(urlEncode(key) + "=" + urlEncode(value));
			});

			final HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.stripe.com/v1/checkout/sessions"))
				.header("Authorization", "Bearer " + apiKey)
				.header("Accept", "application/json")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(BodyPublishers.ofString(body.toString()))
				.build();

			final HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) return null;
			return JsonParser.parseString(response.body()).getAsJsonObject();
		} catch (IOException | InterruptedException | IllegalArgumentException ignored) {
			return null;
		}
	}

	private static String getString(JsonObject object, String key) {
		if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return null;
		try {
			return object.get(key).getAsString();
		} catch (Exception ignored) {
			return null;
		}
	}

	private static String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static String firstNonBlank(String first, String second, String third, String fourth) {
		if (first != null && !first.isBlank()) return first;
		if (second != null && !second.isBlank()) return second;
		if (third != null && !third.isBlank()) return third;
		if (fourth != null && !fourth.isBlank()) return fourth;
		return null;
	}
}
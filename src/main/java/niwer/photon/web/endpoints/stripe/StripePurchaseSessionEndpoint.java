package niwer.photon.web.endpoints.stripe;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectPackProduct;
import niwer.photon.objects.ObjectPurchaseToken;
import niwer.photon.sql.PackProductTable;
import niwer.photon.sql.PurchaseTokenTable;
import niwer.photon.web.endpoints.IEndpoint;

public class StripePurchaseSessionEndpoint implements IEndpoint {

	@Override public String path() { return "/stripe/purchase_session"; }

	@Override public HttpMethod method() { return HttpMethod.POST; }

	@Override
	public void handle(Context handler) {
		final String checkoutSessionId = firstNonBlank(handler.formParam("checkoutSessionId"), handler.formParam("token"), handler.queryParam("checkoutSessionId"), handler.queryParam("token"));
		if (checkoutSessionId == null || checkoutSessionId.isBlank()) {
			handler.status(400).result("Missing checkout session id");
			return;
		}

		final String apiKey = stripeApiKey();
		if (apiKey == null || apiKey.isBlank()) {
			handler.status(500).result("stripe_api_key is not configured");
			return;
		}

		final JsonObject checkoutSession = resolveCheckoutSession(apiKey, checkoutSessionId);
		if (checkoutSession == null) {
			handler.status(502).result("Unable to resolve checkout session");
			return;
		}

		final String purchaseReference = StripeSupport.firstNonBlank(
			StripeSupport.getString(checkoutSession, "client_reference_id"),
			StripeSupport.getString(StripeSupport.getObject(checkoutSession, "metadata"), "purchase_token"),
			StripeSupport.getString(StripeSupport.getObject(checkoutSession, "metadata"), "purchaseToken"),
			StripeSupport.getString(checkoutSession, "id")
		);

		final StripeSupport.CustomerPayload customerPayload = StripeSupport.resolveCustomer(
			apiKey,
			StripeSupport.getString(checkoutSession, "customer"),
			StripeSupport.getObject(checkoutSession, "metadata"),
			StripeSupport.firstNonBlank(StripeSupport.getString(StripeSupport.getObject(checkoutSession, "customer_details"), "email"), StripeSupport.getString(checkoutSession, "customer_email"))
		);

		final String priceId = firstNonBlank(StripeSupport.getString(checkoutSession, "price_id"), StripeSupport.getString(checkoutSession, "price"));
		final String customerName = StripeSupport.firstNonBlank(customerPayload.name(), StripeSupport.getString(StripeSupport.getObject(checkoutSession, "customer_details"), "name"));
		final String contentType = StripeSupport.firstNonBlank(
			StripeSupport.getString(StripeSupport.getObject(checkoutSession, "metadata"), "type"),
			StripeSupport.getString(StripeSupport.getObject(checkoutSession, "metadata"), "content_type"),
			StripeSupport.getString(checkoutSession, "mode")
		);
		final ObjectPackProduct pack = priceId == null ? null : PackProductTable.getByStripePriceId(priceId);

		final ObjectPurchaseToken purchase = ensurePurchaseRecord(purchaseReference, checkoutSessionId, priceId, customerPayload.email(), customerName, contentType, pack);
		if (purchase == null) {
			handler.status(500).result("Failed to seed purchase session");
			return;
		}

		final Map<String, Object> response = new LinkedHashMap<>();
		response.put("purchaseToken", purchase.purchaseToken());
		response.put("checkoutSessionId", purchase.checkoutSessionId());
		response.put("status", purchase.status());
		response.put("customerEmail", purchase.customerEmail());
		response.put("customerName", purchase.customerName());
		response.put("contentType", contentType);
		handler.json(response);
	}

	protected JsonObject resolveCheckoutSession(String apiKey, String checkoutSessionId) {
		return StripeSupport.resolveCheckoutSessionById(apiKey, checkoutSessionId);
	}

	protected String stripeApiKey() {
		return Directories.getConfig().stripe_api_key;
	}

	protected ObjectPurchaseToken ensurePurchaseRecord(String purchaseReference, String checkoutSessionId, String priceId, String customerEmail, String customerName, String contentType, ObjectPackProduct pack) {
		if (pack != null || "one-time-pack".equalsIgnoreCase(contentType)) {
			return PurchaseTokenTable.ensurePendingPurchase(purchaseReference, checkoutSessionId, priceId, customerEmail, customerName);
		}
		return PurchaseTokenTable.ensurePendingPurchase(purchaseReference, checkoutSessionId, priceId, customerEmail, customerName);
	}

	private static String firstNonBlank(String first, String second, String third, String fourth) {
		if (first != null && !first.isBlank()) return first;
		if (second != null && !second.isBlank()) return second;
		if (third != null && !third.isBlank()) return third;
		if (fourth != null && !fourth.isBlank()) return fourth;
		return null;
	}

	private static String firstNonBlank(String first, String second) {
		if (first != null && !first.isBlank()) return first;
		if (second != null && !second.isBlank()) return second;
		return null;
	}
}
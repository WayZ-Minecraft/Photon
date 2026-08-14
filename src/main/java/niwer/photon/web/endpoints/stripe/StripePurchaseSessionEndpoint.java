package niwer.photon.web.endpoints.stripe;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectPurchase;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.util.GsonUtils;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.EndpointUtils;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * This endpoint is used to retrieve the purchase session information for a given Stripe checkout session ID.
 * This endpoint is typically called after a successful Stripe checkout when the user register/logs in.
 * 
 * @author Niwer
 */
public class StripePurchaseSessionEndpoint implements IEndpoint {

	@Override public String path() { return "/stripe/purchase_session"; }

	@Override public HttpMethod method() { return HttpMethod.POST; }

	@Override
	public void handle(Context handler) {
		final String checkoutSessionId = EndpointUtils.firstNonBlank(handler.formParam("checkoutSessionId"), handler.formParam("token"), handler.queryParam("checkoutSessionId"), handler.queryParam("token"));
		if (checkoutSessionId == null || checkoutSessionId.isBlank()) {
			handler.status(400).result("Missing checkout session id");
			return;
		}

		/* Retrieve the Stripe API key from the configuration */
		final String apiKey = Directories.getConfig().stripe_api_key;
		if (apiKey == null || apiKey.isBlank()) {
			handler.status(500).result("stripe_api_key is not configured");
			return;
		}

		/* Resolve the checkout session by its ID */
		final JsonObject checkoutSession = StripeSupport.resolveCheckoutSessionById(apiKey, checkoutSessionId);
		if (checkoutSession == null) {
			handler.status(502).result("Unable to resolve checkout session");
			return;
		}

		/* Determine the purchase reference */
		final String purchaseReference = EndpointUtils.firstNonBlank(
			GsonUtils.getString(checkoutSession, "client_reference_id"),
			GsonUtils.getString(GsonUtils.getObject(checkoutSession, "metadata"), "purchase_token"),
			GsonUtils.getString(GsonUtils.getObject(checkoutSession, "metadata"), "purchaseToken"),
			GsonUtils.getString(checkoutSession, "id")
		);

		/* Resolve the customer information */
		final StripeSupport.CustomerPayload customerPayload = StripeSupport.resolveCustomer(
			apiKey,
			GsonUtils.getString(checkoutSession, "customer"),
			GsonUtils.getObject(checkoutSession, "metadata"),
			EndpointUtils.firstNonBlank(GsonUtils.getString(GsonUtils.getObject(checkoutSession, "customer_details"), "email"), GsonUtils.getString(checkoutSession, "customer_email"))
		);

		final String customerName = EndpointUtils.firstNonBlank(customerPayload.name(), GsonUtils.getString(GsonUtils.getObject(checkoutSession, "customer_details"), "name"));

		/* Ensure the purchase record exists */
		final ObjectPurchase purchase = PurchaseTable.createOrRetrievePendingPurchase(purchaseReference, checkoutSessionId, customerPayload.email(), customerName);
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
		handler.json(response);
	}
}
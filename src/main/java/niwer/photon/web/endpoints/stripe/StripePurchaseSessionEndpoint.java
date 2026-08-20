package niwer.photon.web.endpoints.stripe;

import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectPurchase;
import niwer.photon.objects.stripe.StripeCheckoutSession;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.api.stripe.StripeGetCheckoutSessionByIdRequest;
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
		IEndpoint.setupRateLimit(handler, 15, TimeUnit.SECONDS);

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
		final StripeCheckoutSession checkoutSession = new StripeGetCheckoutSessionByIdRequest(checkoutSessionId).request();
		if (checkoutSession == null) {
			handler.status(502).result("Unable to resolve checkout session");
			return;
		}

		/* Ensure the purchase record exists */
		final ObjectPurchase purchase = PurchaseTable.createOrRetrievePendingPurchase(checkoutSession.clientRefId(), checkoutSessionId, checkoutSession.customerDetails().email(), checkoutSession.customerDetails().name());
		if (purchase == null) {
			handler.status(500).result("Failed to seed purchase session");
			return;
		}
		handler.json(purchase.payload());
	}
}
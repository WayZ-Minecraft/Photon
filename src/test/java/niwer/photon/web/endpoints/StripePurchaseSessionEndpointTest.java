package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import niwer.photon.objects.ObjectPurchaseToken;

class StripePurchaseSessionEndpointTest {

	@Test
	void seedsPurchaseFromCheckoutSessionId() {
		final ContextStubTest stub = new ContextStubTest().formParam("checkoutSessionId", "cs_test_123");
		final ObjectPurchaseToken seededPurchase = new ObjectPurchaseToken(
			"ph_test_token",
			"cs_test_123",
			null,
			"alice@example.com",
			"Alice",
			null,
			null,
			"PENDING",
			null,
			new Date(),
			new Date(),
			null,
			null
		);

		final var endpoint = new niwer.photon.web.endpoints.stripe.StripePurchaseSessionEndpoint() {
			@Override
			protected String stripeApiKey() {
				return "sk_test_123";
			}

			@Override
			protected JsonObject resolveCheckoutSession(String apiKey, String checkoutSessionId) {
				assertEquals("sk_test_123", apiKey);
				assertEquals("cs_test_123", checkoutSessionId);
				final JsonObject checkoutSession = new JsonObject();
				checkoutSession.addProperty("id", "cs_test_123");
				final JsonObject customerDetails = new JsonObject();
				customerDetails.addProperty("email", "alice@example.com");
				customerDetails.addProperty("name", "Alice");
				checkoutSession.add("customer_details", customerDetails);
				return checkoutSession;
			}

			@Override
			protected ObjectPurchaseToken ensurePurchaseRecord(String purchaseReference, String checkoutSessionId, String priceId, String customerEmail, String customerName) {
				assertEquals("cs_test_123", purchaseReference);
				assertEquals("cs_test_123", checkoutSessionId);
				assertEquals("alice@example.com", customerEmail);
				assertEquals("Alice", customerName);
				return seededPurchase;
			}
		};

		endpoint.handle(stub.context());

		assertNotNull(stub.jsonBody());
		final Map<?, ?> response = (Map<?, ?>) stub.jsonBody();
		assertEquals("ph_test_token", response.get("purchaseToken"));
		assertEquals("cs_test_123", response.get("checkoutSessionId"));
	}
}
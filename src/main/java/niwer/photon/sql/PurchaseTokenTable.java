package niwer.photon.sql;

import java.security.SecureRandom;
import java.util.Date;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.objects.ObjectPurchaseToken;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.util.PhotonLogTypes;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class PurchaseTokenTable extends Table {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final String TOKEN_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

	public PurchaseTokenTable(DataBase db) {
		super(db);

		this.addColumns(
			createColumn(db, "purchase_token", EnumColumnTypes.TEXT).primaryKey(),
			createColumn(db, "checkout_session_id", EnumColumnTypes.TEXT).unique(),
			createColumn(db, "price_id", EnumColumnTypes.TEXT),
			createColumn(db, "customer_email", EnumColumnTypes.TEXT),
			createColumn(db, "customer_name", EnumColumnTypes.TEXT),
			createColumn(db, "stripe_customer_id", EnumColumnTypes.TEXT),
			createColumn(db, "stripe_subscription_id", EnumColumnTypes.TEXT),
			createColumn(db, "status", EnumColumnTypes.TEXT).notNull().defaultValue("PENDING"),
			createColumn(db, "linked_account_uuid", EnumColumnTypes.TEXT),
			createColumn(db, "created_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP"),
			createColumn(db, "updated_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP"),
			createColumn(db, "redeemed_at", EnumColumnTypes.DATE_TIME),
			createColumn(db, "expires_at", EnumColumnTypes.DATE_TIME)
		).execute();
	}

	@Override public String name() { return "PurchaseToken"; }

	public static String generateToken() {
		final StringBuilder token = new StringBuilder("ph_");
		for (int group = 0; group < 3; group++) {
			if (group > 0) token.append('-');
			for (int index = 0; index < 4; index++) {
				token.append(TOKEN_ALPHABET.charAt(RANDOM.nextInt(TOKEN_ALPHABET.length())));
			}
		}
		return token.toString();
	}

	public static ObjectPurchaseToken createPendingPurchase(String purchaseToken, String checkoutSessionId, String priceId, String customerEmail, String customerName) {
		final String normalizedToken = normalizeToken(purchaseToken);
		final Date now = new Date();
		InsertionManager.insert(PhotonEngine.DATA_BASE, PurchaseTokenTable.class, "purchase_token", "checkout_session_id", "price_id", "customer_email", "customer_name", "status", "created_at", "updated_at")
			.row(normalizedToken, checkoutSessionId, priceId, normalizeEmail(customerEmail), customerName, "PENDING", now, now)
			.execute();
		return getByToken(normalizedToken);
	}

	public static ObjectPurchaseToken ensurePendingPurchase(String purchaseToken, String checkoutSessionId, String priceId, String customerEmail, String customerName) {
		final ObjectPurchaseToken current = getByPurchaseReference(purchaseToken);
		if (current != null) return current;

		final String normalizedToken = normalizeToken(purchaseToken);
		if (normalizedToken == null || normalizedToken.isBlank()) return null;

		final String normalizedCheckoutSessionId = checkoutSessionId == null || checkoutSessionId.isBlank() ? normalizedToken : checkoutSessionId.trim();
		final Date now = new Date();
		InsertionManager.insert(PhotonEngine.DATA_BASE, PurchaseTokenTable.class, "purchase_token", "checkout_session_id", "price_id", "customer_email", "customer_name", "status", "created_at", "updated_at")
			.row(normalizedToken, normalizedCheckoutSessionId, priceId, normalizeEmail(customerEmail), customerName, "PENDING", now, now)
			.execute();
		return getByToken(normalizedToken);
	}

	public static ObjectPurchaseToken completePurchase(String purchaseToken, String checkoutSessionId, String stripeCustomerId, String stripeSubscriptionId, String customerEmail, String customerName, String status, Date expiresAt) {
		ObjectPurchaseToken current = getByPurchaseReference(purchaseToken);
		if (current == null) {
			current = ensurePendingPurchase(purchaseToken, checkoutSessionId, null, customerEmail, customerName);
		}
		if (current == null) return null;

		UpdateManager.update(PhotonEngine.DATA_BASE, PurchaseTokenTable.class)
			.set("checkout_session_id", checkoutSessionId)
			.set("stripe_customer_id", stripeCustomerId)
			.set("stripe_subscription_id", stripeSubscriptionId)
			.set("customer_email", normalizeEmail(customerEmail != null && !customerEmail.isBlank() ? customerEmail : current.customerEmail()))
			.set("customer_name", customerName != null && !customerName.isBlank() ? customerName : current.customerName())
			.set("status", status == null || status.isBlank() ? current.status() : status)
			.set("expires_at", expiresAt)
			.set("updated_at", new Date())
			.where(Expression.of("purchase_token").isEqualTo(normalizeToken(purchaseToken)))
			.execute();

		final ObjectPurchaseToken updated = getByToken(normalizeToken(purchaseToken));
		if (updated != null && updated.linkedAccountUuid() != null && !updated.linkedAccountUuid().isBlank() && updated.stripeSubscriptionId() != null && !updated.stripeSubscriptionId().isBlank()) {
			SubscriptionTable.upsertSubscription(
				updated.customerEmail(),
				updated.customerName(),
				updated.stripeCustomerId(),
				updated.stripeSubscriptionId(),
				SubscriptionStatus.fromString(updated.status()),
				updated.expiresAt(),
				updated.linkedAccountUuid()
			);
		}

		return updated;
	}

	public static ObjectPurchaseToken getByToken(String purchaseToken) {
		if (purchaseToken == null || purchaseToken.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, PurchaseTokenTable.class)
			.where(Expression.of("purchase_token").isEqualTo(normalizeToken(purchaseToken)))
			.limit(1)
			.executeSerializable(ObjectPurchaseToken.class);
	}

	public static ObjectPurchaseToken getByCheckoutSessionId(String checkoutSessionId) {
		if (checkoutSessionId == null || checkoutSessionId.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, PurchaseTokenTable.class)
			.where(Expression.of("checkout_session_id").isEqualTo(checkoutSessionId.trim()))
			.limit(1)
			.executeSerializable(ObjectPurchaseToken.class);
	}

	private static ObjectPurchaseToken getByPurchaseReference(String purchaseReference) {
		final ObjectPurchaseToken byToken = getByToken(purchaseReference);
		if (byToken != null) return byToken;
		return getByCheckoutSessionId(purchaseReference);
	}

	public static boolean canRedeem(String purchaseToken) {
		final ObjectPurchaseToken token = getByPurchaseReference(purchaseToken);
		return token != null && (token.linkedAccountUuid() == null || token.linkedAccountUuid().isBlank());
	}

	public static boolean redeem(String purchaseToken, ObjectPlayerAccount account) {
		if (purchaseToken == null || purchaseToken.isBlank() || account == null) return false;

		final ObjectPurchaseToken token = getByPurchaseReference(purchaseToken);
		if (token == null) return false;
		if (token.linkedAccountUuid() != null && !token.linkedAccountUuid().isBlank() && !token.linkedAccountUuid().equals(account.getUuid())) return false;

		if (token.stripeSubscriptionId() != null && !token.stripeSubscriptionId().isBlank()) {
			final SubscriptionStatus status = SubscriptionStatus.fromString(token.status());
			final ObjectSubscription subscription = SubscriptionTable.upsertSubscription(
				token.customerEmail(),
				token.customerName(),
				token.stripeCustomerId(),
				token.stripeSubscriptionId(),
				status,
				token.expiresAt(),
				account.getUuid()
			);

			if (subscription == null) {
				Console.log("Failed to link purchase token '" + purchaseToken + "' to subscription '" + token.stripeSubscriptionId() + "'")
					.type(PhotonLogTypes.SQL)
					.error()
					.container(PhotonEngine.LOGGER)
					.send();
				return false;
			}
		}

		UpdateManager.update(PhotonEngine.DATA_BASE, PurchaseTokenTable.class)
			.set("linked_account_uuid", account.getUuid())
			.set("redeemed_at", new Date())
			.set("status", token.stripeSubscriptionId() == null || token.stripeSubscriptionId().isBlank() ? "LINKED_PENDING" : "LINKED")
			.set("updated_at", new Date())
			.where(Expression.of("purchase_token").isEqualTo(normalizeToken(purchaseToken)))
			.execute();

		return true;
	}

	private static String normalizeToken(String purchaseToken) {
		return purchaseToken == null ? null : purchaseToken.trim();
	}

	private static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}
}
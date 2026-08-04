package niwer.photon.sql;

import java.util.Date;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPurchase;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.photon.util.PhotonLogTypes;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class PurchaseTable extends Table {

	public PurchaseTable(DataBase db) {
		super(db);

		this.addColumns(
			createColumn(db, "purchase_token", EnumColumnTypes.TEXT).primaryKey(),
			createColumn(db, "checkout_session_id", EnumColumnTypes.TEXT).unique(),
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

	@Override public String name() { return "Purchase"; }

	public static ObjectPurchase createOrRetrievePendingPurchase(String purchaseToken, String checkoutSessionId, String customerEmail, String customerName) {
		final ObjectPurchase current = getByPurchaseReference(purchaseToken);
		if (current != null) return current;

		final String normalizedToken = normalizeToken(purchaseToken);
		if (normalizedToken == null || normalizedToken.isBlank()) return null;

		final String normalizedCheckoutSessionId = checkoutSessionId == null || checkoutSessionId.isBlank() ? normalizedToken : checkoutSessionId.trim();
		final Date now = new Date();
		InsertionManager.insert(PhotonEngine.DATA_BASE, PurchaseTable.class, "purchase_token", "checkout_session_id", "customer_email", "customer_name", "status", "created_at", "updated_at")
			.row(normalizedToken, normalizedCheckoutSessionId, normalizeEmail(customerEmail), customerName, "PENDING", now, now)
			.execute();
		return getByToken(normalizedToken);
	}

	/**
	 * Completes a purchase by updating the corresponding record in the database with the provided details.
	 * If the purchase record does not exist, it attempts to create a pending purchase first.
	 * After updating, if the purchase is linked to an account and has a subscription ID, it also updates or creates the corresponding subscription record.
	 * 
	 * @param purchaseToken The purchase token associated with the purchase to complete.
	 * @param checkoutSessionId The checkout session ID associated with the purchase to complete.
	 * @param stripeCustomerId The Stripe customer ID associated with the purchase to complete.
	 * @param stripeSubscriptionId The Stripe subscription ID associated with the purchase to complete.
	 * @param customerEmail The email address of the customer associated with the purchase to complete.
	 * @param customerName The name of the customer associated with the purchase to complete.
	 * @param status The status to update the purchase to.
	 * @param expiresAt The date at which the purchase expires.
	 * @return The updated purchase record, or null if the update failed.
	 */
	public static ObjectPurchase completePurchase(String purchaseToken, String checkoutSessionId, String stripeCustomerId, String stripeSubscriptionId, String customerEmail, String customerName, String status, Date expiresAt) {
		ObjectPurchase current = getByPurchaseReference(purchaseToken);
		if (current == null) {
			current = createOrRetrievePendingPurchase(purchaseToken, checkoutSessionId, customerEmail, customerName);
		}
		if (current == null) return null;

		UpdateManager.update(PhotonEngine.DATA_BASE, PurchaseTable.class)
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

		final ObjectPurchase updated = getByToken(normalizeToken(purchaseToken));
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

	/**
	 * Retrieves a purchase record from the database based on the provided purchase token.
	 * 
	 * @param purchaseToken The purchase token associated with the purchase to retrieve.
	 * @return An ObjectPurchase instance representing the purchase record, or null if no matching record is found.
	 */
	public static ObjectPurchase getByToken(String purchaseToken) {
		if (purchaseToken == null || purchaseToken.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, PurchaseTable.class)
			.where(Expression.of("purchase_token").isEqualTo(normalizeToken(purchaseToken)))
			.limit(1)
			.executeSerializable(ObjectPurchase.class);
	}

	/**
	 * Retrieves a purchase record from the database based on the provided checkout session ID.
	 * 
	 * @param checkoutSessionId The checkout session ID associated with the purchase to retrieve.
	 * @return An ObjectPurchase instance representing the purchase record, or null if no matching record is found.
	 */
	public static ObjectPurchase getByCheckoutSessionId(String checkoutSessionId) {
		if (checkoutSessionId == null || checkoutSessionId.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, PurchaseTable.class)
			.where(Expression.of("checkout_session_id").isEqualTo(checkoutSessionId.trim()))
			.limit(1)
			.executeSerializable(ObjectPurchase.class);
	}

	private static ObjectPurchase getByPurchaseReference(String purchaseReference) {
		final ObjectPurchase byToken = getByToken(purchaseReference);
		if (byToken != null) return byToken;
		return getByCheckoutSessionId(purchaseReference);
	}

	/**
	 * Checks if a purchase token can be redeemed. A token can be redeemed if it exists and is not already linked to an account.
	 * 
	 * @param purchaseToken The purchase token to check for redemption eligibility.
	 * @return True if the token can be redeemed; false otherwise.
	 */
	public static boolean canRedeem(String purchaseToken) {
		final ObjectPurchase token = getByPurchaseReference(purchaseToken);
		return token != null && (token.linkedAccountUuid() == null || token.linkedAccountUuid().isBlank());
	}

	/**
	 * Attempts to redeem a purchase token for a given user account. The token is linked to the account if it exists and is not already linked to another account.
	 * 
	 * @param purchaseToken The purchase token to redeem.
	 * @param account The user account for which to redeem the token.
	 * @return True if the token was successfully redeemed; false otherwise.
	 */
	public static boolean redeem(String purchaseToken, ObjectUserAccount account) {
		if (purchaseToken == null || purchaseToken.isBlank() || account == null) return false;

		final ObjectPurchase token = getByPurchaseReference(purchaseToken);
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

		UpdateManager.update(PhotonEngine.DATA_BASE, PurchaseTable.class)
			.set("linked_account_uuid", account.getUuid())
			.set("redeemed_at", new Date())
			.set("status", token.stripeSubscriptionId() == null || token.stripeSubscriptionId().isBlank() ? "LINKING_PENDING" : "LINKED")
			.set("updated_at", new Date())
			.where(Expression.of("purchase_token").isEqualTo(normalizeToken(purchaseToken)))
			.execute();

		return true;
	}

	private static String normalizeToken(String purchaseToken) { return purchaseToken == null ? null : purchaseToken.trim(); }

	private static String normalizeEmail(String email) { return email == null ? null : email.trim().toLowerCase(); }
}
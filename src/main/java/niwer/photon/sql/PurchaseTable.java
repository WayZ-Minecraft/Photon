package niwer.photon.sql;

import java.util.Date;
import java.util.List;

import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionListLineItemsParams;

import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPurchase;
import niwer.photon.util.stripe.StripeHelper;
import niwer.photon.util.stripe.StripePurchaseStatus;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.Table;

public class PurchaseTable extends Table {

    public PurchaseTable(DataBase db) {
        super(db);
        this.addColumnsFromClass(ObjectPurchase.class).execute();
    }

    @Override public String name() { return "Purchase"; }

    public static ObjectPurchase createOrRetrievePendingPurchase(String purchaseToken, String checkoutSessionId, String customerEmail, String customerName, String productId) {
        final ObjectPurchase current = getByPurchaseReference(purchaseToken);
        if (current != null) return current;

        final String normalizedToken = normalizeToken(purchaseToken);
        if (normalizedToken == null || normalizedToken.isBlank()) return null;

        final String normalizedCheckoutSessionId = checkoutSessionId == null || checkoutSessionId.isBlank() ? normalizedToken : checkoutSessionId.trim();
        final Date now = new Date();
        InsertionManager.insert(PhotonEngine.DATA_BASE, PurchaseTable.class, "purchase_token", "checkout_session_id", "customer_email", "customer_name", "product_id", "status", "created_at", "updated_at")
            .row(normalizedToken, normalizedCheckoutSessionId, normalizeEmail(customerEmail), customerName, productId, StripePurchaseStatus.PENDING, now, now)
            .execute();
        return getByToken(normalizedToken);
    }

	public static ObjectPurchase upsertCompletedPurchase(Session session) {
        if (session == null || session.getId() == null) return null;

        Price price = null;
        try {
            final var lineItems = session.listLineItems(SessionListLineItemsParams.builder().setLimit(1L).build());
            if (lineItems != null && !lineItems.getData().isEmpty()) {
                price = lineItems.getData().get(0).getPrice();
            }
        } catch (Exception ignored) {}

        final Customer customer = session.getCustomerObject();
        final var details = session.getCustomerDetails();

        final String email = customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()
            ? customer.getEmail()
            : (details != null ? details.getEmail() : null);

        final String name = customer != null && customer.getName() != null && !customer.getName().isBlank()
            ? customer.getName()
            : (details != null ? details.getName() : null);

        final String customerId = customer != null ? customer.getId() : session.getCustomer();
        final StripePurchaseStatus status = "paid".equalsIgnoreCase(session.getPaymentStatus())
            ? StripePurchaseStatus.ACTIVE
            : StripePurchaseStatus.PENDING;

        final String token = session.getClientReferenceId() != null && !session.getClientReferenceId().isBlank()
            ? session.getClientReferenceId()
            : session.getId();

        final String githubUsername = session.getMetadata() != null ? session.getMetadata().get("github_username") : null;

        return upsertCompletedPurchase(token, session.getId(), customerId, null, email, name, status, null, githubUsername, StripeHelper.resolveProductId(price));
    }

	public static ObjectPurchase upsertCompletedPurchase(String purchaseToken, String checkoutSessionId, String stripeCustomerId, String stripeSubscriptionId, String customerEmail, String customerName, StripePurchaseStatus status, Date expiresAt, String githubUsername, String productId) {
		final String token = purchaseToken != null && !purchaseToken.isBlank() ? normalizeToken(purchaseToken) : normalizeToken(checkoutSessionId);
		if (token == null || token.isBlank()) return null;

		ObjectPurchase existing = getByPurchaseReference(token);
		if (existing == null && checkoutSessionId != null && !checkoutSessionId.isBlank()) {
			existing = getByCheckoutSessionId(checkoutSessionId);
		}

		if (existing == null) {
			final Date now = new Date();
			InsertionManager.insert(PhotonEngine.DATA_BASE, PurchaseTable.class,
					"purchase_token", "checkout_session_id", "stripe_customer_id", "stripe_subscription_id",
					"customer_email", "customer_name", "product_id", "status", "expires_at", "github_username", "created_at", "updated_at")
				.row(token, checkoutSessionId, stripeCustomerId, stripeSubscriptionId,
					normalizeEmail(customerEmail), customerName, productId, status, expiresAt, githubUsername, now, now)
				.execute();
			return getByToken(token);
		}

		return completePurchase(token, checkoutSessionId, stripeCustomerId, stripeSubscriptionId, customerEmail, customerName, status, expiresAt, githubUsername, productId);
	}

    public static ObjectPurchase completePurchase(String purchaseToken, String checkoutSessionId, String stripeCustomerId, String stripeSubscriptionId, String customerEmail, String customerName, StripePurchaseStatus status, Date expiresAt, String githubUsername, String productId) {
        ObjectPurchase current = getByPurchaseReference(purchaseToken);
        if (current == null) current = createOrRetrievePendingPurchase(purchaseToken, checkoutSessionId, customerEmail, customerName, productId);
        if (current == null) return null;

        UpdateManager.update(PhotonEngine.DATA_BASE, PurchaseTable.class)
            .set("checkout_session_id", checkoutSessionId)
            .set("stripe_customer_id", stripeCustomerId)
            .set("stripe_subscription_id", stripeSubscriptionId)
            .set("customer_email", normalizeEmail(customerEmail != null && !customerEmail.isBlank() ? customerEmail : current.customerEmail()))
            .set("customer_name", customerName != null && !customerName.isBlank() ? customerName : current.customerName())
            .set("product_id", productId != null && !productId.isBlank() ? productId : current.productId())
            .set("status", status == null ? current.status() : status)
            .set("expires_at", expiresAt)
            .set("updated_at", new Date())
            .set("github_username", githubUsername != null && !githubUsername.isBlank() ? githubUsername : current.githubUsername())
            .where(Expression.of("purchase_token").isEqualTo(normalizeToken(purchaseToken)))
            .execute();

        return getByToken(normalizeToken(purchaseToken));
    }

    public static ObjectPurchase getByCustomerId(String stripeCustomerId) {
        if (stripeCustomerId == null || stripeCustomerId.isBlank()) return null;
        return SelectionManager.select(PhotonEngine.DATA_BASE, PurchaseTable.class)
            .where(Expression.of("stripe_customer_id").isEqualTo(stripeCustomerId.trim()))
            .limit(1)
            .executeSerializable(ObjectPurchase.class);
    }

    public static ObjectPurchase getByToken(String purchaseToken) {
        if (purchaseToken == null || purchaseToken.isBlank()) return null;
        return SelectionManager.select(PhotonEngine.DATA_BASE, PurchaseTable.class)
            .where(Expression.of("purchase_token").isEqualTo(normalizeToken(purchaseToken)))
            .limit(1)
            .executeSerializable(ObjectPurchase.class);
    }

    public static ObjectPurchase getByCheckoutSessionId(String checkoutSessionId) {
        if (checkoutSessionId == null || checkoutSessionId.isBlank()) return null;
        return SelectionManager.select(PhotonEngine.DATA_BASE, PurchaseTable.class)
            .where(Expression.of("checkout_session_id").isEqualTo(checkoutSessionId.trim()))
            .limit(1)
            .executeSerializable(ObjectPurchase.class);
    }

    public static List<ObjectPurchase> getByAccountUuid(String accountUuid) {
        if (accountUuid == null || accountUuid.isBlank()) return List.of();
        return SelectionManager.select(PhotonEngine.DATA_BASE, PurchaseTable.class)
            .where(Expression.of("linked_account_uuid").isEqualTo(accountUuid))
            .executeList(ObjectPurchase.class);
    }

    public static ObjectPurchase getByPurchaseReference(String purchaseReference) {
        final ObjectPurchase byToken = getByToken(purchaseReference);
        if (byToken != null) return byToken;
        return getByCheckoutSessionId(purchaseReference);
    }

    public static boolean canRedeem(String purchaseToken) {
        final ObjectPurchase token = getByPurchaseReference(purchaseToken);
        return token != null && (token.linkedAccountUuid() == null || token.linkedAccountUuid().isBlank());
    }

    public static boolean markAsRedeemed(String purchaseToken, String accountUuid) {
        if (purchaseToken == null || purchaseToken.isBlank() || accountUuid == null || accountUuid.isBlank()) {
            return false;
        }
        UpdateManager.update(PhotonEngine.DATA_BASE, PurchaseTable.class)
            .set("linked_account_uuid", accountUuid)
            .set("redeemed_at", new Date())
            .set("status", StripePurchaseStatus.ACTIVE)
            .set("updated_at", new Date())
            .where(Expression.of("purchase_token").isEqualTo(normalizeToken(purchaseToken)))
            .execute();
        return true;
    }

    private static String normalizeToken(String purchaseToken) { return purchaseToken == null ? null : purchaseToken.trim(); }

    private static String normalizeEmail(String email) { return email == null ? null : email.trim().toLowerCase(); }
}
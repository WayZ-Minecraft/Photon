package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectPurchaseToken extends SQLSerializable<ObjectPurchaseToken> {

	@IColumnField(name = "purchase_token", primaryKey = true, notNull = true)
	private String purchaseToken;

	@IColumnField(name = "checkout_session_id", unique = true)
	private String checkoutSessionId;

	@IColumnField(name = "price_id")
	private String priceId;

	@IColumnField(name = "customer_email")
	private String customerEmail;

	@IColumnField(name = "customer_name")
	private String customerName;

	@IColumnField(name = "stripe_customer_id")
	private String stripeCustomerId;

	@IColumnField(name = "stripe_subscription_id")
	private String stripeSubscriptionId;

	@IColumnField(name = "status", notNull = true)
	private String status;

	@IColumnField(name = "linked_account_uuid")
	private String linkedAccountUuid;

	@IColumnField(name = "created_at")
	private Date createdAt = new Date();

	@IColumnField(name = "updated_at")
	private Date updatedAt = new Date();

	@IColumnField(name = "redeemed_at")
	private Date redeemedAt;

	@IColumnField(name = "expires_at")
	private Date expiresAt;

	public ObjectPurchaseToken() {}

	public ObjectPurchaseToken(String purchaseToken, String checkoutSessionId, String priceId, String customerEmail, String customerName, String stripeCustomerId, String stripeSubscriptionId, String status, String linkedAccountUuid, Date createdAt, Date updatedAt, Date redeemedAt, Date expiresAt) {
		this.purchaseToken = purchaseToken;
		this.checkoutSessionId = checkoutSessionId;
		this.priceId = priceId;
		this.customerEmail = customerEmail;
		this.customerName = customerName;
		this.stripeCustomerId = stripeCustomerId;
		this.stripeSubscriptionId = stripeSubscriptionId;
		this.status = status;
		this.linkedAccountUuid = linkedAccountUuid;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.redeemedAt = redeemedAt;
		this.expiresAt = expiresAt;
	}

	public String purchaseToken() { return purchaseToken; }
	public String checkoutSessionId() { return checkoutSessionId; }
	public String priceId() { return priceId; }
	public String customerEmail() { return customerEmail; }
	public String customerName() { return customerName; }
	public String stripeCustomerId() { return stripeCustomerId; }
	public String stripeSubscriptionId() { return stripeSubscriptionId; }
	public String status() { return status; }
	public String linkedAccountUuid() { return linkedAccountUuid; }
	public Date createdAt() { return createdAt; }
	public Date updatedAt() { return updatedAt; }
	public Date redeemedAt() { return redeemedAt; }
	public Date expiresAt() { return expiresAt; }
}
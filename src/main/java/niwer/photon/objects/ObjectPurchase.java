package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectPurchase extends SQLSerializable<ObjectPurchase> {

	@IColumnField(name = "purchase_token", primaryKey = true, notNull = true)
	private String purchaseToken;

	@IColumnField(name = "checkout_session_id", unique = true)
	private String checkoutSessionId;

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

	public ObjectPurchase() {}
	
	public String purchaseToken() { return purchaseToken; }

	public String checkoutSessionId() { return checkoutSessionId; }

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
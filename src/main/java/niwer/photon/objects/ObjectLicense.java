package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectLicense extends SQLSerializable<ObjectLicense> {
	@IColumnField(name = "license_key", primaryKey = true, notNull = true)
	private String licenseKey;

	@IColumnField(name = "product_id", notNull = true)
	private String productId;

	@IColumnField(name = "customer_name")
	private String customerName;

	@IColumnField(name = "customer_email")
	private String customerEmail;

	@IColumnField(name = "order_id")
	private String orderId;

	@IColumnField(name = "hwid")
	private String hwid;

	@IColumnField(name = "status", notNull = true)
	private String status;

	@IColumnField(name = "created_at")
	private Date createdAt = new Date();

	@IColumnField(name = "activated_at")
	private Date activatedAt;

	@IColumnField(name = "expires_at")
	private Date expiresAt;

	public ObjectLicense() {}

	public ObjectLicense(String licenseKey, String productId, String customerName, String customerEmail, String orderId, String status, Date expiresAt) {
		this.licenseKey = licenseKey;
		this.productId = productId;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.orderId = orderId;
		this.status = status;
		this.expiresAt = expiresAt;
	}

	public boolean isExpired() { return this.expiresAt != null && this.expiresAt.before(new Date()); }

	public String licenseKey() { return this.licenseKey; }

	public String productId() { return this.productId; }

	public String customerName() { return this.customerName; }

	public String customerEmail() { return this.customerEmail; }

	public String orderId() { return this.orderId; }

	public String hwid() { return this.hwid; }

	public String status() { return this.status; }

	public Date createdAt() { return this.createdAt; }

	public Date activatedAt() { return this.activatedAt; }

	public Date expiresAt() { return this.expiresAt; }
}
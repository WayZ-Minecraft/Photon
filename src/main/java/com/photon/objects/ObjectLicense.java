package com.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectLicense extends SQLSerializable<ObjectLicense> {
	@IColumnField(name = "license_key", primaryKey = true, notNull = true)
	public String licenseKey;

	@IColumnField(name = "product_id", notNull = true)
	public String productId;

	@IColumnField(name = "customer_name")
	public String customerName;

	@IColumnField(name = "customer_email")
	public String customerEmail;

	@IColumnField(name = "tebex_order_id")
	public String tebexOrderId;

	@IColumnField(name = "hwid")
	public String hwid;

	@IColumnField(name = "status", notNull = true)
	public String status;

	@IColumnField(name = "created_at")
	public Date createdAt = new Date();

	@IColumnField(name = "activated_at")
	public Date activatedAt;

	@IColumnField(name = "expires_at")
	public Date expiresAt;

	public ObjectLicense() {}

	public ObjectLicense(String licenseKey, String productId, String customerName, String customerEmail, String tebexOrderId, String status, Date expiresAt) {
		this.licenseKey = licenseKey;
		this.productId = productId;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.tebexOrderId = tebexOrderId;
		this.status = status;
		this.expiresAt = expiresAt;
	}

	public boolean isExpired() {
		return this.expiresAt != null && this.expiresAt.before(new Date());
	}
}
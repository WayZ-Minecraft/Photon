package niwer.photon.objects;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectLicense extends SQLSerializable<ObjectLicense> {
	@IColumnField(name = "license_key", primaryKey = true, notNull = true)
	private String licenseKey;

	@IColumnField(name = "product_id", notNull = true)
	private String productId;

	@IColumnField(name = "name")
	private String name;

	@IColumnField(name = "customer_email")
	private String customerEmail;

	@IColumnField(name = "creator_uuid")
	private String creatorUuid;

	@IColumnField(name = "hwid")
	private String hwid;

	@IColumnField(name = "status", notNull = true)
	private String status;

	@IColumnField(name = "created_at")
	private String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

	@IColumnField(name = "activated_at")
	private Date activatedAt;

	@IColumnField(name = "expires_at")
	private Date expiresAt;

	public ObjectLicense() {}

	public ObjectLicense(String licenseKey, String productId, String name, String customerEmail, String creatorUuid, String status, Date expiresAt) {
		this.licenseKey = licenseKey;
		this.productId = productId;
		this.name = name;
		this.customerEmail = customerEmail;
		this.creatorUuid = creatorUuid;
		this.status = status;
		this.expiresAt = expiresAt;
	}

	public boolean isExpired() { return this.expiresAt != null && this.expiresAt.before(new Date()); }

	public String licenseKey() { return this.licenseKey; }

	public String productId() { return this.productId; }

	public String name() { return this.name; }

	public String customerEmail() { return this.customerEmail; }

	public String creatorUuid() { return this.creatorUuid; }

	public String hwid() { return this.hwid; }

	public String status() { return this.status; }

	public Date createdAt() {
		return parseDate(this.createdAt);
	}

	public Date activatedAt() { return this.activatedAt; }

	public Date expiresAt() { return this.expiresAt; }

	private static Date parseDate(String value) {
		if (value == null || value.isBlank()) return null;
		if ("CURRENT_TIMESTAMP".equalsIgnoreCase(value)) return null;

		for (DateTimeFormatter formatter : new DateTimeFormatter[] {
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
		}) {
			try {
				return Date.from(LocalDateTime.parse(value, formatter).atZone(ZoneId.systemDefault()).toInstant());
			} catch (DateTimeParseException ignored) {
				// try the next format
			}
		}

		return null;
	}
}
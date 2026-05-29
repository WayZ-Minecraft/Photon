package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectSubscription extends SQLSerializable<ObjectSubscription> {

    @IColumnField(name = "customer_email", primaryKey = true, notNull = true)
    private String customerEmail;

    @IColumnField(name = "customer_name")
    private String customerName;

    @IColumnField(name = "tebex_customer_id")
    private String tebexCustomerId;

    @IColumnField(name = "tebex_subscription_id", unique = true)
    private String tebexSubscriptionId;

    @IColumnField(name = "status", notNull = true)
    private String status = "ACTIVE";

    @IColumnField(name = "expires_at")
    private Date expiresAt;

    @IColumnField(name = "updated_at")
    private Date updatedAt;

    public ObjectSubscription() {}

    public ObjectSubscription(String customerEmail, String customerName, String tebexCustomerId, String tebexSubscriptionId, String status, Date expiresAt, Date updatedAt) {
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.tebexCustomerId = tebexCustomerId;
        this.tebexSubscriptionId = tebexSubscriptionId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return status != null && status.equalsIgnoreCase("ACTIVE") && (expiresAt == null || expiresAt.after(new Date()));
    }

    public String customerEmail() { return customerEmail; }
    public String customerName() { return customerName; }
    public String tebexCustomerId() { return tebexCustomerId; }
    public String tebexSubscriptionId() { return tebexSubscriptionId; }
    public String status() { return status; }
    public Date expiresAt() { return expiresAt; }
    public Date updatedAt() { return updatedAt; }
}
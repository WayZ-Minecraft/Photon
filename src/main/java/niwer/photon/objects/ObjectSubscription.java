package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectSubscription extends SQLSerializable<ObjectSubscription> {

    @IColumnField(name = "id", primaryKey = true, autoIncrement = true)
    private int id;

    @IColumnField(name = "customer_email", notNull = true)
    private String customerEmail;

    @IColumnField(name = "account_uuid")
    private String accountUuid;

    @IColumnField(name = "customer_name")
    private String customerName;

    @IColumnField(name = "customer_id")
    private String customerId;

    @IColumnField(name = "subscription_id", unique = true)
    private String subscriptionId;

    @IColumnField(name = "status", notNull = true)
    private String status = "ACTIVE";

    @IColumnField(name = "expires_at")
    private Date expiresAt;

    @IColumnField(name = "updated_at")
    private Date updatedAt;

    public ObjectSubscription() {}

    public boolean isActive() { return status != null && status.equalsIgnoreCase("ACTIVE") && (expiresAt == null || expiresAt.after(new Date())); }

    public String customerEmail() { return customerEmail; }

    public String accountUuid() { return accountUuid; }

    public String customerName() { return customerName; }

    public String customerId() { return customerId; }

    public String subscriptionId() { return subscriptionId; }

    public String status() { return status; }

    public Date expiresAt() { return expiresAt; }
    
    public Date updatedAt() { return updatedAt; }
}
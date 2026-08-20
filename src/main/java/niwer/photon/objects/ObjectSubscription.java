package niwer.photon.objects;

import java.util.Date;

import niwer.photon.sql.SubscriptionTable;
import niwer.photon.sql.SubscriptionTable.SubscriptionStatus;
import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;
import niwer.queryon.tables.api.IDefaultValue;

/**
 * @author Niwer 
 */
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

    @IColumnField(name = "status", notNull = true, defaultValue = @IDefaultValue(value = "ACTIVE"))
    private SubscriptionStatus status;

    @IColumnField(name = "expires_at")
    private Date expiresAt;

    @IColumnField(name = "updated_at", defaultValue = @IDefaultValue(value = "CURRENT_TIMESTAMP"))
    private Date updatedAt;

    public ObjectSubscription() {}

    public boolean isActive() { return SubscriptionStatus.ACTIVE == status && (expiresAt == null || expiresAt.after(new Date())); }

    public String customerEmail() { return customerEmail; }

    public String accountUuid() { return accountUuid; }

    public String customerName() { return customerName; }

    public String customerId() { return customerId; }

    public String subscriptionId() { return subscriptionId; }

    public SubscriptionTable.SubscriptionStatus status() { return status; }

    public Date expiresAt() { return expiresAt; }
    
    public Date updatedAt() { return updatedAt; }
}
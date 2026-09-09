package niwer.photon.objects;

import java.util.Date;

import niwer.photon.util.stripe.StripePurchaseStatus;
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

    @IColumnField(name = "product_id")
    private String productId;

    @IColumnField(name = "customer_id")
    private String customerId;

    @IColumnField(name = "subscription_id", unique = true)
    private String subscriptionId;

    @IColumnField(name = "status", notNull = true, defaultValue = @IDefaultValue(value = "ACTIVE"))
    private StripePurchaseStatus status;

    @IColumnField(name = "expires_at")
    private Date expiresAt;

    @IColumnField(name = "updated_at", defaultValue = @IDefaultValue(value = "CURRENT_TIMESTAMP"))
    private Date updatedAt;

    public ObjectSubscription() {}

    public boolean isActive() { return StripePurchaseStatus.ACTIVE == status && (expiresAt == null || expiresAt.after(new Date())); }

    public int id() { return id; }

    public String customerEmail() { return customerEmail; }

    public String accountUuid() { return accountUuid; }

    public String customerName() { return customerName; }

    public String productId() { return productId; }

    public String customerId() { return customerId; }

    public String subscriptionId() { return subscriptionId; }

    public StripePurchaseStatus status() { return status; }

    public Date expiresAt() { return expiresAt; }
    
    public Date updatedAt() { return updatedAt; }
}
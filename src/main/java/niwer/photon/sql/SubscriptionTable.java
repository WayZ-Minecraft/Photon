package niwer.photon.sql;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectSubscription;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.Table;

public class SubscriptionTable extends Table {

    public static enum SubscriptionStatus {
        ACTIVE,
        PENDING,
        CANCELED,
        LINKING_PENDING,
        LINKED,
        EXPIRED;
    }

    public SubscriptionTable(DataBase db) {
        super(db);
        this.addColumnsFromClass(ObjectSubscription.class).execute();
    }

    @Override public String name() { return "Subscription"; }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static ObjectSubscription getByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return SelectionManager.select(PhotonEngine.DATA_BASE, SubscriptionTable.class)
            .where(Expression.of("customer_email").isEqualTo(normalizeEmail(email)))
            .limit(1)
            .executeSerializable(ObjectSubscription.class);
    }

    public static ObjectSubscription getFirstByAccountUuid(String accountUuid) {
        if (accountUuid == null || accountUuid.isBlank()) return null;
        return SelectionManager.select(PhotonEngine.DATA_BASE, SubscriptionTable.class)
            .where(Expression.of("account_uuid").isEqualTo(accountUuid))
            .limit(1)
            .executeSerializable(ObjectSubscription.class);
    }

    public static ObjectSubscription getBySubscriptionId(String subscriptionId) {
        if (subscriptionId == null || subscriptionId.isBlank()) return null;
        return SelectionManager.select(PhotonEngine.DATA_BASE, SubscriptionTable.class)
            .where(Expression.of("subscription_id").isEqualTo(subscriptionId))
            .limit(1)
            .executeSerializable(ObjectSubscription.class);
    }

    public static ObjectSubscription getByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) return null;
        return SelectionManager.select(PhotonEngine.DATA_BASE, SubscriptionTable.class)
            .where(Expression.of("customer_id").isEqualTo(customerId))
            .limit(1)
            .executeSerializable(ObjectSubscription.class);
    }

    public static List<ObjectSubscription> getByAccountUuid(String accountUuid) {
        if (accountUuid == null || accountUuid.isBlank()) return List.of();
        return SelectionManager.select(PhotonEngine.DATA_BASE, SubscriptionTable.class)
            .where(Expression.of("account_uuid").isEqualTo(accountUuid))
            .executeList(ObjectSubscription.class);
    }

    public static List<ObjectSubscription> getAllActive() {
        return SelectionManager.select(PhotonEngine.DATA_BASE, SubscriptionTable.class)
            .where(Expression.of("status").isEqualTo(SubscriptionStatus.ACTIVE))
            .executeList(ObjectSubscription.class);
    }

    public static ObjectSubscription upsertSubscription(String email, String customerName, String customerId, String subscriptionId, SubscriptionStatus status, Date expiresAt) {
        return upsertSubscription(email, customerName, customerId, subscriptionId, status, expiresAt, null);
    }

    public static ObjectSubscription upsertSubscription(String email, String customerName, String customerId, String subscriptionId, SubscriptionStatus status, Date expiresAt, String accountUuid) {
        return upsertSubscription(email, customerName, customerId, subscriptionId, status, expiresAt, accountUuid, null);
    }

    public static ObjectSubscription upsertSubscription(String email, String customerName, String customerId, String subscriptionId, SubscriptionStatus status, Date expiresAt, String accountUuid, String productId) {
        final String normalizedEmail = normalizeEmail(email);
        final Date updatedAt = new Date();
        final ObjectSubscription current = getBySubscriptionId(subscriptionId);
        final ObjectSubscription currentByCustomerId = current == null ? getByCustomerId(customerId) : null;
        final ObjectSubscription currentByAccountUuid = current == null && currentByCustomerId == null ? getByAccountUuid(accountUuid).stream().filter(subscription -> productId == null || productId.equals(subscription.productId())).findFirst().orElse(null) : null;
        final ObjectSubscription existing = current != null ? current : (currentByCustomerId != null ? currentByCustomerId : currentByAccountUuid);
        if (existing == null && (subscriptionId == null || subscriptionId.isBlank())) return null;
        final String nextAccountUuid = accountUuid != null && !accountUuid.isBlank() ? accountUuid : (existing == null ? null : existing.accountUuid());

        if (existing == null) {
            InsertionManager.insert(PhotonEngine.DATA_BASE, SubscriptionTable.class, "customer_email", "account_uuid", "customer_name", "customer_id", "subscription_id", "product_id", "status", "expires_at", "updated_at")
                .row(normalizedEmail, nextAccountUuid, customerName, customerId, subscriptionId, productId, status.name(), expiresAt, updatedAt)
                .execute();
        } else {
            UpdateManager.update(PhotonEngine.DATA_BASE, SubscriptionTable.class)
                .set("customer_email", normalizedEmail)
                .set("account_uuid", nextAccountUuid)
                .set("customer_name", customerName)
                .set("customer_id", customerId)
				.set("product_id", productId != null && !productId.isBlank() ? productId : existing.productId())
				.set("subscription_id", subscriptionId != null && !subscriptionId.isBlank() ? subscriptionId : existing.subscriptionId())
                .set("status", status.name())
                .set("expires_at", expiresAt)
                .set("updated_at", updatedAt)
                .where(Expression.of(existing.subscriptionId() != null && !existing.subscriptionId().isBlank() ? "subscription_id" : "id").isEqualTo(existing.subscriptionId() != null && !existing.subscriptionId().isBlank() ? existing.subscriptionId() : existing.id()))
                .execute();
        }

            return existing == null ? getBySubscriptionId(subscriptionId) : existing;
    }

    public static boolean isActive(String email, String accountUuid) {
        final ObjectSubscription subscription = resolveSubscription(email, accountUuid);
        return subscription != null && subscription.isActive();
    }


    public static List<Map<String, Object>> entitlements(String accountUuid) {
        final List<Map<String, Object>> subscriptions = getByAccountUuid(accountUuid).stream()
            .map(item -> entitlementRecord(item.productId(), item.status(), item.expiresAt() == null ? null : item.expiresAt().getTime(), null))
            .toList();
        final List<Map<String, Object>> purchases = PurchaseTable.getByAccountUuid(accountUuid).stream()
            .map(item -> entitlementRecord(item.productId(), item.status(), item.expiresAt() == null ? null : item.expiresAt().getTime(), item.createdAt() == null ? null : item.createdAt().getTime()))
            .toList();
        return java.util.stream.Stream.concat(
            subscriptions.stream().map(item -> entitlement(item, "SUBSCRIPTION")),
            purchases.stream().map(item -> entitlement(item, "ONE_TIME"))
        ).toList();
    }

    private static Map<String, Object> entitlementRecord(String productId, SubscriptionStatus status, Long expiresAt, Long createdAt) {
        final Map<String, Object> record = new LinkedHashMap<>();
        record.put("productId", productId);
        record.put("status", status);
        if (expiresAt != null) record.put("expiresAt", expiresAt);
        if (createdAt != null) record.put("createdAt", createdAt);
        return record;
    }

    private static Map<String, Object> entitlement(Map<String, Object> item, String type) {
        final Map<String, Object> record = new LinkedHashMap<>(item);
        record.put("type", type);
        return record;
    }

    public static boolean hasAccess(String email, String accountUuid, String productId) {
        if (productId == null || productId.isBlank()) return false;
        final boolean subscriptionAccess = getByAccountUuid(accountUuid).stream().anyMatch(subscription -> productId.equals(subscription.productId()) && subscription.isActive());
        final boolean purchaseAccess = PurchaseTable.getByAccountUuid(accountUuid).stream().anyMatch(purchase -> productId.equals(purchase.productId())
            && purchase.status() == SubscriptionStatus.ACTIVE
            && (purchase.expiresAt() == null || purchase.expiresAt().after(new Date()))
        );
        return subscriptionAccess || purchaseAccess;
    }

    public static boolean hasAnyAccess(String accountUuid) {
        return getByAccountUuid(accountUuid).stream().anyMatch(ObjectSubscription::isActive) || PurchaseTable.getByAccountUuid(accountUuid).stream().anyMatch(purchase -> purchase.status() == SubscriptionStatus.ACTIVE);
    }

    private static ObjectSubscription resolveSubscription(String email, String accountUuid) {
        final ObjectSubscription subscriptionByUuid = accountUuid != null && !accountUuid.isBlank() ? getFirstByAccountUuid(accountUuid) : null;
        if (subscriptionByUuid != null) return subscriptionByUuid;
        return getByEmail(email);
    }
}
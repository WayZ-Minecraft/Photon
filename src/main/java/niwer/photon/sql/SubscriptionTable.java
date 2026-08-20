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

    public static ObjectSubscription getByAccountUuid(String accountUuid) {
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

    public static List<ObjectSubscription> getAllActive() {
        return SelectionManager.select(PhotonEngine.DATA_BASE, SubscriptionTable.class)
            .where(Expression.of("status").isEqualTo(SubscriptionStatus.ACTIVE))
            .executeList(ObjectSubscription.class);
    }

    public static ObjectSubscription upsertSubscription(String email, String customerName, String customerId, String subscriptionId, SubscriptionStatus status, Date expiresAt) {
        return upsertSubscription(email, customerName, customerId, subscriptionId, status, expiresAt, null);
    }

    public static ObjectSubscription upsertSubscription(String email, String customerName, String customerId, String subscriptionId, SubscriptionStatus status, Date expiresAt, String accountUuid) {
        final String normalizedEmail = normalizeEmail(email);
        final Date updatedAt = new Date();
        final ObjectSubscription current = getByEmail(normalizedEmail);
        final ObjectSubscription currentByAccountUuid = current == null ? getByAccountUuid(accountUuid) : null;
        final ObjectSubscription existing = current != null ? current : currentByAccountUuid;
        final String nextAccountUuid = accountUuid != null && !accountUuid.isBlank() ? accountUuid : (existing == null ? null : existing.accountUuid());

        if (existing == null) {
            InsertionManager.insert(PhotonEngine.DATA_BASE, SubscriptionTable.class, "customer_email", "account_uuid", "customer_name", "customer_id", "subscription_id", "status", "expires_at", "updated_at")
                .row(normalizedEmail, nextAccountUuid, customerName, customerId, subscriptionId, status.name(), expiresAt, updatedAt)
                .execute();
        } else {
            UpdateManager.update(PhotonEngine.DATA_BASE, SubscriptionTable.class)
                .set("customer_email", normalizedEmail)
                .set("account_uuid", nextAccountUuid)
                .set("customer_name", customerName)
                .set("customer_id", customerId)
                // .set("subscription_id", subscriptionId) // Unique, so we don't update it to avoid conflicts
                .set("status", status.name())
                .set("expires_at", expiresAt)
                .set("updated_at", updatedAt)
                .where(Expression.of(existing.accountUuid() != null && !existing.accountUuid().isBlank() ? "account_uuid" : "customer_email").isEqualTo(existing.accountUuid() != null && !existing.accountUuid().isBlank() ? existing.accountUuid() : normalizedEmail))
                .execute();
        }

        return getByEmail(normalizedEmail);
    }

    public static boolean isActive(String email) {
        return isActive(email, null);
    }

    public static boolean isActive(String email, String accountUuid) {
        final ObjectSubscription subscription = resolveSubscription(email, accountUuid);
        return subscription != null && subscription.isActive();
    }

    public static Map<String, Object> subscriptionDetails(String email) {
        return subscriptionDetails(email, null);
    }

    public static Map<String, Object> subscriptionDetails(String email, String accountUuid) {
        final Map<String, Object> response = new LinkedHashMap<>();
        final ObjectSubscription subscription = resolveSubscription(email, accountUuid);
        response.put("subscriber", subscription != null && subscription.isActive());
        response.put("subscriptionStatus", subscription == null ? SubscriptionStatus.EXPIRED : subscription.status());
        response.put("subscriptionExpiresAt", subscription == null || subscription.expiresAt() == null ? null : subscription.expiresAt().getTime());
        response.put("subscriptionAccountUuid", subscription == null ? null : subscription.accountUuid());
        return response;
    }

    private static ObjectSubscription resolveSubscription(String email, String accountUuid) {
        final ObjectSubscription subscriptionByUuid = accountUuid != null && !accountUuid.isBlank() ? getByAccountUuid(accountUuid) : null;
        if (subscriptionByUuid != null) return subscriptionByUuid;
        return getByEmail(email);
    }
}
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
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class SubscriptionTable extends Table {

    public enum SubscriptionStatus {
        ACTIVE,
        EXPIRED;

        public static SubscriptionStatus fromString(String value) {
            if (value == null || value.isBlank()) return EXPIRED;
            try { return SubscriptionStatus.valueOf(value.toUpperCase()); }
            catch (IllegalArgumentException e) { return EXPIRED; }
        }
    }

    public SubscriptionTable(DataBase db) {
        super(db);

        this.addColumns(
            createColumn(db, "customer_email", EnumColumnTypes.TEXT).primaryKey(),
            createColumn(db, "customer_name", EnumColumnTypes.TEXT),
            createColumn(db, "tebex_customer_id", EnumColumnTypes.TEXT),
            createColumn(db, "tebex_subscription_id", EnumColumnTypes.TEXT).unique(),
            createColumn(db, "status", EnumColumnTypes.TEXT).notNull().defaultValue(SubscriptionStatus.ACTIVE.name()),
            createColumn(db, "expires_at", EnumColumnTypes.DATE_TIME),
            createColumn(db, "updated_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP")
        ).execute();
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

    public static List<ObjectSubscription> getAllActive() {
        return SelectionManager.select(PhotonEngine.DATA_BASE, SubscriptionTable.class)
            .where(Expression.of("status").isEqualTo(SubscriptionStatus.ACTIVE.name()))
            .executeList(ObjectSubscription.class);
    }

    public static ObjectSubscription upsertSubscription(String email, String customerName, String tebexCustomerId, String tebexSubscriptionId, SubscriptionStatus status, Date expiresAt) {
        final String normalizedEmail = normalizeEmail(email);
        final Date updatedAt = new Date();
        final ObjectSubscription current = getByEmail(normalizedEmail);

        if (current == null) {
            InsertionManager.insert(PhotonEngine.DATA_BASE, SubscriptionTable.class, "customer_email", "customer_name", "tebex_customer_id", "tebex_subscription_id", "status", "expires_at", "updated_at")
                .row(normalizedEmail, customerName, tebexCustomerId, tebexSubscriptionId, status.name(), expiresAt, updatedAt)
                .execute();
        } else {
            UpdateManager.update(PhotonEngine.DATA_BASE, SubscriptionTable.class)
                .set("customer_name", customerName)
                .set("tebex_customer_id", tebexCustomerId)
                .set("tebex_subscription_id", tebexSubscriptionId)
                .set("status", status.name())
                .set("expires_at", expiresAt)
                .set("updated_at", updatedAt)
                .where(Expression.of("customer_email").isEqualTo(normalizedEmail))
                .execute();
        }

        return getByEmail(normalizedEmail);
    }

    public static boolean isActive(String email) {
        final ObjectSubscription subscription = getByEmail(email);
        return subscription != null && subscription.isActive();
    }

    public static Map<String, Object> subscriptionDetails(String email) {
        final Map<String, Object> response = new LinkedHashMap<>();
        final ObjectSubscription subscription = getByEmail(email);
        response.put("subscriber", subscription != null && subscription.isActive());
        response.put("subscriptionStatus", subscription == null ? SubscriptionStatus.EXPIRED.name() : subscription.status());
        response.put("subscriptionExpiresAt", subscription == null || subscription.expiresAt() == null ? null : subscription.expiresAt().getTime());
        return response;
    }
}
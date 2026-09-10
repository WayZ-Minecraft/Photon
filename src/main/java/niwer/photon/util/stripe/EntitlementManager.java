package niwer.photon.util.stripe;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import niwer.photon.objects.ObjectPurchase;
import niwer.photon.objects.ObjectSubscription;
import niwer.photon.objects.ObjectUserAccount;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.sql.SubscriptionTable;

public final class EntitlementManager {

    private EntitlementManager() {}

    public static boolean hasAccess(String accountUuid, String productId) {
        if (accountUuid == null || accountUuid.isBlank() || productId == null || productId.isBlank()) return false;

        final boolean HAS_ACCESS = SubscriptionTable.getByAccountUuid(accountUuid).stream().anyMatch(sub -> productId.equals(sub.productId()) && sub.isActive()); // TODO CHECK ONE TIME PURCHASES AS WELL
        if (HAS_ACCESS) return true;

        return PurchaseTable.getByAccountUuid(accountUuid).stream().anyMatch(p -> productId.equals(p.productId())
            && p.status() == StripePurchaseStatus.ACTIVE
            && (p.expiresAt() == null || p.expiresAt().after(new Date())));
    }

    public static boolean hasAnyAccess(String accountUuid) {
        if (accountUuid == null || accountUuid.isBlank()) return false;

        final boolean hasSub = SubscriptionTable.getByAccountUuid(accountUuid).stream().anyMatch(ObjectSubscription::isActive);
        if (hasSub) return true;

        return PurchaseTable.getByAccountUuid(accountUuid).stream().anyMatch(p -> p.status() == StripePurchaseStatus.ACTIVE && (p.expiresAt() == null || p.expiresAt().after(new Date())));
    }

    public static List<Map<String, Object>> getEntitlements(String accountUuid) {
        if (accountUuid == null || accountUuid.isBlank()) return List.of();

        final List<Map<String, Object>> subscriptions = SubscriptionTable.getByAccountUuid(accountUuid).stream()
            .map(item -> entitlementRecord(item.productId(), item.status(), item.expiresAt() == null ? null : item.expiresAt().getTime(), null))
            .map(item -> entitlement(item, "SUBSCRIPTION"))
            .toList();

        final List<Map<String, Object>> purchases = PurchaseTable.getByAccountUuid(accountUuid).stream()
            .map(item -> entitlementRecord(item.productId(), item.status(), item.expiresAt() == null ? null : item.expiresAt().getTime(), item.createdAt() == null ? null : item.createdAt().getTime()))
            .map(item -> entitlement(item, "ONE_TIME"))
            .toList();

        return Stream.concat(subscriptions.stream(), purchases.stream()).toList();
    }

    public static boolean redeemPurchase(String purchaseToken, ObjectUserAccount account) {
        if (purchaseToken == null || purchaseToken.isBlank() || account == null) return false;

        final ObjectPurchase purchase = PurchaseTable.getByPurchaseReference(purchaseToken);
        if (purchase == null) return false;
        
        if (purchase.linkedAccountUuid() != null && !purchase.linkedAccountUuid().isBlank() && !purchase.linkedAccountUuid().equals(account.getUuid())) {
            return false;
        }

        // Link the purchase record first
        final boolean marked = PurchaseTable.markAsRedeemed(purchaseToken, account.getUuid());
        if (!marked) return false;

        // Link subscription if one exists for this customer or email
        ObjectSubscription sub = null;
        if (purchase.stripeCustomerId() != null && !purchase.stripeCustomerId().isBlank()) {
            sub = SubscriptionTable.getByCustomerId(purchase.stripeCustomerId());
        }
        if (sub == null && purchase.customerEmail() != null && !purchase.customerEmail().isBlank()) {
            sub = SubscriptionTable.getByEmail(purchase.customerEmail());
        }

        if (sub != null) {
            SubscriptionTable.upsertSubscription(
                sub.customerEmail(),
                sub.customerName(),
                sub.customerId(),
                sub.subscriptionId(),
                sub.status(),
                sub.expiresAt(),
                account.getUuid(),
                sub.productId()
            );
        }

        return true;
    }

    private static Map<String, Object> entitlementRecord(String productId, StripePurchaseStatus status, Long expiresAt, Long createdAt) {
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
}
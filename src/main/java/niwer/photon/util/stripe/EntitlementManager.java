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

        final boolean HAS_SUB_ACCESS = SubscriptionTable.getByAccountUuid(accountUuid).stream().anyMatch(sub -> productId.equals(sub.productId()) && sub.isActive());
        if (HAS_SUB_ACCESS) return true;

        return PurchaseTable.getByAccountUuid(accountUuid).stream().anyMatch(p -> productId.equals(p.productId())
            && p.status() == StripePurchaseStatus.ACTIVE
            && (p.expiresAt() == null || p.expiresAt().after(new Date())));
    }

    public static boolean hasAnyAccess(String accountUuid) {
        if (accountUuid == null || accountUuid.isBlank()) return false;

        final boolean HAS_SUB = SubscriptionTable.getByAccountUuid(accountUuid).stream().anyMatch(ObjectSubscription::isActive);
        if (HAS_SUB) return true;

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

        // if (purchase.stripeSubscriptionId() != null && !purchase.stripeSubscriptionId().isBlank()) { //TODO
        //     SubscriptionTable.upsertSubscription(
        //         purchase.customerEmail(),
        //         purchase.customerName(),
        //         purchase.stripeCustomerId(),
        //         purchase.stripeSubscriptionId(),
        //         purchase.status(),
        //         purchase.expiresAt(),
        //         account.getUuid(),
        //         purchase.productId()
        //     );
        // }

        return PurchaseTable.markAsRedeemed(purchaseToken, account.getUuid());
    }

    private static Map<String, Object> entitlementRecord(String productId, StripePurchaseStatus status, Long expiresAt, Long createdAt) {
        final Map<String, Object> RECORD = new LinkedHashMap<>();
        RECORD.put("productId", productId);
        RECORD.put("status", status);
        if (expiresAt != null) RECORD.put("expiresAt", expiresAt);
        if (createdAt != null) RECORD.put("createdAt", createdAt);
        return RECORD;
    }

    private static Map<String, Object> entitlement(Map<String, Object> item, String type) {
        final Map<String, Object> RECORD = new LinkedHashMap<>(item);
        RECORD.put("type", type);
        return RECORD;
    }
}
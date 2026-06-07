package niwer.photon.web.endpoints;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.util.license.LicenseClaims;
import niwer.photon.util.license.LicenseManager;
import niwer.photon.util.license.LicenseValidationResult;

public class LicenseValidateEndpoint implements IEndpoint {

    @Override public String path() { return "/licenses/validate"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final Map<String, Object> body = parseBody(handler.body());

        final String licenseKey = getString(handler, body, "license_key", "licenseKey", "key");
        if (licenseKey == null || licenseKey.isBlank()) {
            handler.status(400).result("Missing license key");
            return;
        }

        final String expectedProductId = firstNonBlank(
            getString(handler, body, "product_id", "productId"),
            Directories.getConfig().license_product_id
        );
        if (expectedProductId == null || expectedProductId.isBlank()) {
            handler.status(400).result("Missing product id");
            return;
        }

        final String hardwareId = getString(handler, body, "hardware_id", "hardwareId", "hwid");
        if (hardwareId == null || hardwareId.isBlank()) {
            handler.status(400).result("Missing hwid");
            return;
        }

        final LicenseValidationResult result = LicenseManager.validateLicense(licenseKey, expectedProductId, hardwareId);
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("valid", result.valid());
        payload.put("reason", result.reason() == null ? null : result.reason().name());
        payload.put("message", result.message());
        payload.put("claims", toClaimsPayload(result.claims()));

        handler.status(200).json(payload);
    }
    
    private static Map<String, Object> parseBody(String rawBody) {
        try {
            if (rawBody == null || rawBody.isBlank()) return Map.of();
            final var type = new TypeToken<Map<String, Object>>() {}.getType();
            final Map<String, Object> parsed = Directories.GSON.fromJson(rawBody, type);
            return parsed == null ? Map.of() : parsed;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static String getString(Context handler, Map<String, Object> body, String... keys) {
        for (String key : keys) {
            final String formValue = handler.formParam(key);
            if (formValue != null && !formValue.isBlank()) return formValue;

            final String queryValue = handler.queryParam(key);
            if (queryValue != null && !queryValue.isBlank()) return queryValue;

            final Object bodyValue = body.get(key);
            if (bodyValue != null) {
                final String value = String.valueOf(bodyValue).trim();
                if (!value.isBlank()) return value;
            }
        }
        return null;
    }

    private static String firstNonBlank(String first, String fallback) {
        if (first != null && !first.isBlank()) return first;
        if (fallback != null && !fallback.isBlank()) return fallback;
        return null;
    }

    private static Map<String, Object> toClaimsPayload(LicenseClaims claims) {
        if (claims == null) return null;

        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("license_id", claims.license_id());
        payload.put("product_id", claims.product_id());
        payload.put("customer_name", claims.customer_name());
        payload.put("customer_email", claims.customer_email());
        payload.put("hardware_id", claims.hardware_id());
        payload.put("issued_at", claims.issued_at());
        payload.put("expires_at", claims.expires_at());
        return payload;
    }
}

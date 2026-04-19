package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonSyntaxException;

import niwer.photon.Directories;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.sql.tables.LicenseTableTest;

class LicenseEndpointTest {

    @AfterEach
    void resetState() {
        LicenseTableTest.reset();
        Directories.config = Directories.getConfig();
    }

    @Test
    void exposesTheExpectedPathAndMethod() {
        final var endpoint = new niwer.photon.web.endpoints.tebex.LicenseEndpoint();

        assertEquals("/tebex/license", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.POST, endpoint.method());
    }

    @Test
    void rejectsUnauthorizedRequests() {
        Directories.config = new Directories.NetworkConfig();
        Directories.config.tebex_webhook_secret = "top-secret";

        final ContextStubTest stub = new ContextStubTest().body("{}");

        new niwer.photon.web.endpoints.tebex.LicenseEndpoint().handle(stub.context());

        assertEquals(401, stub.statusCode());
        assertEquals("Unauthorized", stub.resultBody());
    }

    @Test
    void acceptsBlankConfiguredSecret() {
        Directories.config = new Directories.NetworkConfig();
        Directories.config.tebex_webhook_secret = "";

        final ContextStubTest stub = new ContextStubTest()
            .requestHeader("X-Photon-Secret", "anything")
            .body("{\"product_id\":\"prod-1\"}");

        new niwer.photon.web.endpoints.tebex.LicenseEndpoint().handle(stub.context());

        assertEquals(200, stub.statusCode());
        assertNotNull(stub.jsonBody());
    }

    @Test
    void returnsInvalidJsonWhenBodyIsNull() {
        Directories.config = new Directories.NetworkConfig();
        Directories.config.tebex_webhook_secret = "";

        final ContextStubTest stub = new ContextStubTest().body("null");

        assertThrows(JsonSyntaxException.class, () -> new niwer.photon.web.endpoints.tebex.LicenseEndpoint().handle(stub.context()));
    }

    @Test
    void returnsInvalidJsonWhenBodyCannotBeParsed() {
        Directories.config = new Directories.NetworkConfig();
        Directories.config.tebex_webhook_secret = "";

        final ContextStubTest stub = new ContextStubTest().body("not-json");

        assertThrows(JsonSyntaxException.class, () -> new niwer.photon.web.endpoints.tebex.LicenseEndpoint().handle(stub.context()));
    }

    @Test
    void usesPrimaryFieldNamesAndExplicitExpiresAt() {
        Directories.config = new Directories.NetworkConfig();
        Directories.config.tebex_webhook_secret = "top-secret";
        Directories.config.license_product_id = "default-product";
        Directories.config.license_default_duration_days = 30L;

        final long expiresAt = System.currentTimeMillis() + 123_456L;
        final ContextStubTest stub = new ContextStubTest()
            .requestHeader("X-Photon-Secret", "top-secret")
            .body("""
                {
                  "product_id": "product-a",
                  "customer_name": "Alice",
                  "customer_email": "alice@example.com",
                  "tebex_order_id": "order-1",
                  "expires_at": %d,
                  "duration_days": 0
                }
                """.formatted(expiresAt));

        new niwer.photon.web.endpoints.tebex.LicenseEndpoint().handle(stub.context());

        assertEquals(200, stub.statusCode());
        final ObjectLicense license = (ObjectLicense) stub.jsonBody();
        assertEquals("product-a", license.productId());
        assertEquals("Alice", license.customerName());
        assertEquals("alice@example.com", license.customerEmail());
        assertEquals("order-1", license.tebexOrderId());
        assertEquals(new Date(expiresAt), license.expiresAt());
        assertTrue(license.licenseKey().matches("[A-HJ-NP-Z2-9]{5}(?:-[A-HJ-NP-Z2-9]{5}){3}"));
        assertEquals(LicenseTableTest.normalizeKey(LicenseTableTest.lastIssueLicenseKey()), license.licenseKey());
        assertEquals("product-a", LicenseTableTest.lastIssueProductId());
    }

    @Test
    void usesQuerySecretSecondaryFieldNamesAndDefaultDuration() {
        Directories.config = new Directories.NetworkConfig();
        Directories.config.tebex_webhook_secret = "top-secret";
        Directories.config.license_product_id = "default-product";
        Directories.config.license_default_duration_days = 2L;

        final ContextStubTest stub = new ContextStubTest()
            .queryParam("secret", "top-secret")
            .body("""
                {
                  "productId": "product-b",
                  "customerName": "Bob",
                  "customerEmail": "bob@example.com",
                  "order_id": "order-2",
                  "durationDays": 2
                }
                """);

        new niwer.photon.web.endpoints.tebex.LicenseEndpoint().handle(stub.context());

        assertEquals(200, stub.statusCode());
        final ObjectLicense license = (ObjectLicense) stub.jsonBody();
        assertEquals("product-b", license.productId());
        assertEquals("Bob", license.customerName());
        assertEquals("bob@example.com", license.customerEmail());
        assertEquals("order-2", license.tebexOrderId());
        assertNotNull(license.expiresAt());
        assertTrue(license.expiresAt().after(new Date()));
    }

    @Test
    void fallsBackToDefaultValuesWhenNumericParsingFails() {
        Directories.config = new Directories.NetworkConfig();
        Directories.config.tebex_webhook_secret = "top-secret";
        Directories.config.license_product_id = "default-product";
        Directories.config.license_default_duration_days = 1L;

        final ContextStubTest stub = new ContextStubTest()
            .requestHeader("X-Photon-Secret", "top-secret")
            .body("""
                {
                  "duration_days": "not-a-number"
                }
                """);

        new niwer.photon.web.endpoints.tebex.LicenseEndpoint().handle(stub.context());

        assertEquals(200, stub.statusCode());
        final ObjectLicense license = (ObjectLicense) stub.jsonBody();
        assertEquals("default-product", license.productId());
        assertEquals("", license.customerName());
        assertEquals("", license.customerEmail());
        assertEquals("", license.tebexOrderId());
        assertNotNull(license.expiresAt());
    }
}

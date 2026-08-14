package niwer.photon.web.endpoints;

import java.util.Map;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.util.license.LicenseManager;
import niwer.photon.util.license.LicenseValidationResult;

/**
 * Endpoint for validating a license key against a product ID and hardware ID.
 * It checks if the provided license key is valid for the specified product and hardware, returning the validation result along with any associated claims.
 */
public class LicenseValidateEndpoint implements IEndpoint {

    @Override public String path() { return "/licenses/validate"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final Map<String, Object> body = EndpointUtils.parseBody(handler.body());

        /* Get the license key */
        final String licenseKey = EndpointUtils.getString(handler, body, "license_key", "licenseKey", "key");
        if (licenseKey == null || licenseKey.isBlank()) {
            handler.status(400).result("Missing license key");
            return;
        }

        /* Get the product ID */
        final String expectedProductId = EndpointUtils.firstNonBlank(EndpointUtils.getString(handler, body, "product_id", "productId"), Directories.getConfig().license_product_id);
        if (expectedProductId == null || expectedProductId.isBlank()) {
            handler.status(400).result("Missing product id");
            return;
        }

        /* Get the hardware ID */
        final String hardwareId = EndpointUtils.getString(handler, body, "hardware_id", "hardwareId", "hwid");
        if (hardwareId == null || hardwareId.isBlank()) {
            handler.status(400).result("Missing hwid");
            return;
        }

        /* Validate the license */
        final LicenseValidationResult result = LicenseManager.validateLicense(licenseKey, expectedProductId, hardwareId);
        handler.status(200).json(result.payload());
    }
}

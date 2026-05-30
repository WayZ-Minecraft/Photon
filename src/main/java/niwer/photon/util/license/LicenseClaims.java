package niwer.photon.util.license;

/**
 * Represents the claims contained in a valid license key, such as the license ID, product ID, customer information, hardware binding, and expiration date.
 * This is used to extract and represent the information encoded in a license key after it has been validated, allowing the application to make use of the license details such as enforcing hardware binding or displaying customer information.
 * 
 * @author Niwer
 */
public record LicenseClaims(
    String license_id,
    String product_id,
    String customer_name,
    String customer_email,
    String hardware_id,
    Long issued_at,
    Long expires_at,
    String order_id
) {}
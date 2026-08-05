package niwer.photon.sql;

import java.util.Date;
import java.util.List;

import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectLicense;
import niwer.photon.util.PhotonLogTypes;

import niwer.lumen.Console;
import niwer.queryon.DataBase;
import niwer.queryon.QueryonException;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class LicenseTable extends Table {
	public enum LicenseStatus {
		ISSUED,
		ACTIVE,
		REVOKED;
	}

	public LicenseTable(DataBase db) {
		super(db);

		this.addColumns(
			createColumn(db, "license_key", EnumColumnTypes.TEXT).primaryKey(),
			createColumn(db, "product_id", EnumColumnTypes.TEXT).notNull(),
			createColumn(db, "name", EnumColumnTypes.TEXT),
			createColumn(db, "customer_email", EnumColumnTypes.TEXT),
			createColumn(db, "hwid", EnumColumnTypes.TEXT),
			createColumn(db, "creator_uuid", EnumColumnTypes.TEXT),
			createColumn(db, "status", LicenseStatus.class).notNull().defaultValue(LicenseStatus.ISSUED),
			createColumn(db, "created_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP"),
			createColumn(db, "activated_at", EnumColumnTypes.DATE_TIME),
			createColumn(db, "expires_at", EnumColumnTypes.DATE_TIME)
		).execute();
	}

	@Override public String name() { return "License"; }

	public static String normalizeKey(String licenseKey) { return licenseKey == null ? null : licenseKey.trim().toUpperCase(); }

	public static ObjectLicense issueLicense(String licenseKey, String productId, String name, String customerEmail, String creatorUuid, Date expiresAt) {
		final Date createdAt = new Date();
		InsertionManager.insert(PhotonEngine.DATA_BASE, LicenseTable.class, "license_key", "product_id", "name", "customer_email", "creator_uuid", "status", "created_at", "expires_at")
			.row(normalizeKey(licenseKey), productId, name, customerEmail, creatorUuid, LicenseStatus.ISSUED, createdAt, expiresAt)
			.execute();

		return getByKey(licenseKey);
	}

	public static ObjectLicense getByKey(String licenseKey) {
		if (licenseKey == null || licenseKey.isBlank()) return null;
		normalizeLegacyTimestampRows();
		return SelectionManager.select(PhotonEngine.DATA_BASE, LicenseTable.class)
			.where(Expression.of("license_key").isEqualTo(normalizeKey(licenseKey)))
			.limit(1)
			.executeSerializable(ObjectLicense.class);
	}

    

	public static List<ObjectLicense> getByCustomerEmail(String customerEmail) {
		if (customerEmail == null || customerEmail.isBlank()) return List.of();
		normalizeLegacyTimestampRows();
		final String normalizedEmail = customerEmail.trim().toLowerCase();
		return SelectionManager.select(PhotonEngine.DATA_BASE, LicenseTable.class)
			.where(Expression.of("LOWER(customer_email)").isEqualTo(normalizedEmail))
			.executeList(ObjectLicense.class);
	}

	public static List<ObjectLicense> getByCreatorUuid(String creatorUuid) {
		if (creatorUuid == null || creatorUuid.isBlank()) return List.of();
		normalizeLegacyTimestampRows();
		return SelectionManager.select(PhotonEngine.DATA_BASE, LicenseTable.class)
			.where(Expression.of("creator_uuid").isEqualTo(creatorUuid))
			.executeList(ObjectLicense.class);
	}

	public static boolean exists(String licenseKey) { return getByKey(licenseKey) != null; }

	public static boolean activate(String licenseKey, String hwid) {
		if (licenseKey == null || licenseKey.isBlank() || hwid == null || hwid.isBlank()) return false;
		try {
            UpdateManager.update(PhotonEngine.DATA_BASE, LicenseTable.class)
                .set("hwid", hwid)
                .set("status", LicenseStatus.ACTIVE)
                .set("activated_at", new Date())
                .where(Expression.of("license_key").isEqualTo(normalizeKey(licenseKey)))
                .execute();
            return true;
        } catch(QueryonException e) {
            Console.log("Failed to activate license key '" + licenseKey + "': " + e.getMessage()).type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
            return false;
        }
	}

	public static boolean revoke(String licenseKey) {
		if (licenseKey == null || licenseKey.isBlank()) return false;
        try {
            UpdateManager.update(PhotonEngine.DATA_BASE, LicenseTable.class)
                .set("status", LicenseStatus.REVOKED)
                .where(Expression.of("license_key").isEqualTo(normalizeKey(licenseKey)))
                .execute();
            return true;
        } catch(QueryonException e) {
            Console.log("Failed to revoke license key '" + licenseKey + "': " + e.getMessage()).type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
            return false;
        }
	}

	private static void normalizeLegacyTimestampRows() {
		try {
			final Date now = new Date();
			UpdateManager.update(PhotonEngine.DATA_BASE, LicenseTable.class)
				.set("created_at", now)
				.where(Expression.of("created_at").isEqualTo("CURRENT_TIMESTAMP"))
				.execute();
			UpdateManager.update(PhotonEngine.DATA_BASE, LicenseTable.class)
				.set("activated_at", now)
				.where(Expression.of("activated_at").isEqualTo("CURRENT_TIMESTAMP"))
				.execute();
			UpdateManager.update(PhotonEngine.DATA_BASE, LicenseTable.class)
				.set("expires_at", now)
				.where(Expression.of("expires_at").isEqualTo("CURRENT_TIMESTAMP"))
				.execute();
		} catch (QueryonException e) {
			Console.log("Failed to normalize legacy license timestamps: " + e.getMessage()).type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
		}
	}
}
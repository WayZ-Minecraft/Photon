package niwer.photon.sql;

import java.util.Date;

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

		public static LicenseStatus fromString(String value) {
			if (value == null || value.isBlank()) return ISSUED;
			try { return LicenseStatus.valueOf(value.toUpperCase()); }
			catch (IllegalArgumentException e) { return ISSUED; }
		}
	}

	public LicenseTable(DataBase db) {
		super(db);

		this.addColumns(
			createColumn(db, "license_key", EnumColumnTypes.TEXT).primaryKey(),
			createColumn(db, "product_id", EnumColumnTypes.TEXT).notNull(),
			createColumn(db, "customer_name", EnumColumnTypes.TEXT),
			createColumn(db, "customer_email", EnumColumnTypes.TEXT),
			createColumn(db, "tebex_order_id", EnumColumnTypes.TEXT).unique(),
			createColumn(db, "hwid", EnumColumnTypes.TEXT),
			createColumn(db, "status", EnumColumnTypes.TEXT).notNull().defaultValue(LicenseStatus.ISSUED.name()),
			createColumn(db, "created_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP"),
			createColumn(db, "activated_at", EnumColumnTypes.DATE_TIME),
			createColumn(db, "expires_at", EnumColumnTypes.DATE_TIME)
		).execute();
	}

	@Override public String name() { return "License"; }

	public static String normalizeKey(String licenseKey) { return licenseKey == null ? null : licenseKey.trim().toUpperCase(); }

	public static ObjectLicense issueLicense(String licenseKey, String productId, String customerName, String customerEmail, String tebexOrderId, Date expiresAt) {
		InsertionManager.insert(PhotonEngine.DATA_BASE, LicenseTable.class, "license_key", "product_id", "customer_name", "customer_email", "tebex_order_id", "status", "expires_at")
			.row(normalizeKey(licenseKey), productId, customerName, customerEmail, tebexOrderId, LicenseStatus.ISSUED.name(), expiresAt)
			.execute();

		return getByKey(licenseKey);
	}

	public static ObjectLicense getByKey(String licenseKey) {
		if (licenseKey == null || licenseKey.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, LicenseTable.class)
			.where(Expression.of("license_key").isEqualTo(normalizeKey(licenseKey)))
			.limit(1)
			.executeSerializable(ObjectLicense.class);
	}

	public static ObjectLicense getByTebexOrderId(String tebexOrderId) {
		if (tebexOrderId == null || tebexOrderId.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, LicenseTable.class)
			.where(Expression.of("tebex_order_id").isEqualTo(tebexOrderId))
			.limit(1)
			.executeSerializable(ObjectLicense.class);
	}

	public static boolean exists(String licenseKey) { return getByKey(licenseKey) != null; }

	public static boolean activate(String licenseKey, String hwid) {
		if (licenseKey == null || licenseKey.isBlank() || hwid == null || hwid.isBlank()) return false;
		try {
            UpdateManager.update(PhotonEngine.DATA_BASE, LicenseTable.class)
                .set("hwid", hwid)
                .set("status", LicenseStatus.ACTIVE.name())
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
                .set("status", LicenseStatus.REVOKED.name())
                .where(Expression.of("license_key").isEqualTo(normalizeKey(licenseKey)))
                .execute();
            return true;
        } catch(QueryonException e) {
            Console.log("Failed to revoke license key '" + licenseKey + "': " + e.getMessage()).type(PhotonLogTypes.SQL).error().container(PhotonEngine.LOGGER).send();
            return false;
        }
	}
}
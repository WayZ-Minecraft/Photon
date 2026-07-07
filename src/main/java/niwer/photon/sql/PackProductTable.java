package niwer.photon.sql;

import java.io.File;
import java.util.Date;
import java.util.List;

import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPackProduct;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class PackProductTable extends Table {

	public PackProductTable(DataBase db) {
		super(db);

		this.addColumns(
			createColumn(db, "id", EnumColumnTypes.TEXT).primaryKey(),
			createColumn(db, "name", EnumColumnTypes.TEXT).notNull(),
			createColumn(db, "description", EnumColumnTypes.TEXT),
			createColumn(db, "category", EnumColumnTypes.TEXT),
			createColumn(db, "stripe_price_id", EnumColumnTypes.TEXT).notNull().unique(),
			createColumn(db, "file_path", EnumColumnTypes.TEXT).defaultValue("/downloads/{id}.zip"),
			createColumn(db, "version_number", EnumColumnTypes.TEXT).defaultValue("1.0"),
			createColumn(db, "status", EnumColumnTypes.TEXT).defaultValue("ACTIVE"),
			createColumn(db, "created_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP"),
			createColumn(db, "updated_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP")
		).execute();
	}

	@Override public String name() { return "Packs"; }

	public static String normalizeId(String id) {
		return id == null ? null : id.trim();
	}

	public static ObjectPackProduct getById(String id) {
		if (id == null || id.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, PackProductTable.class)
			.where(Expression.of("id").isEqualTo(normalizeId(id)))
			.limit(1)
			.executeSerializable(ObjectPackProduct.class);
	}

	public static ObjectPackProduct getByStripePriceId(String priceId) {
		if (priceId == null || priceId.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, PackProductTable.class)
			.where(Expression.of("stripe_price_id").isEqualTo(priceId.trim()))
			.limit(1)
			.executeSerializable(ObjectPackProduct.class);
	}

	public static List<ObjectPackProduct> getAllActive() {
		return SelectionManager.select(PhotonEngine.DATA_BASE, PackProductTable.class)
			.where(Expression.of("status").isEqualTo("ACTIVE"))
			.executeList(ObjectPackProduct.class);
	}

	public static ObjectPackProduct upsertPack(String id, String name, String description, String category, String stripePriceId, String filePath, String versionNumber, String status) {
		final String normalizedId = normalizeId(id);
		final Date now = new Date();
		final ObjectPackProduct current = getById(normalizedId);
		final String nextFilePath = filePath == null || filePath.isBlank() ? "/downloads/" + normalizedId + ".zip" : filePath;
		final String nextVersion = versionNumber == null || versionNumber.isBlank() ? "1.0" : versionNumber;
		final String nextStatus = status == null || status.isBlank() ? "ACTIVE" : status;

		if (current == null) {
			InsertionManager.insert(PhotonEngine.DATA_BASE, PackProductTable.class, "id", "name", "description", "category", "stripe_price_id", "file_path", "version_number", "status", "created_at", "updated_at")
				.row(normalizedId, name, description, category, stripePriceId, nextFilePath, nextVersion, nextStatus, now, now)
				.execute();
		} else {
			UpdateManager.update(PhotonEngine.DATA_BASE, PackProductTable.class)
				.set("name", name)
				.set("description", description)
				.set("category", category)
				.set("stripe_price_id", stripePriceId)
				.set("file_path", nextFilePath)
				.set("version_number", nextVersion)
				.set("status", nextStatus)
				.set("updated_at", now)
				.where(Expression.of("id").isEqualTo(normalizedId))
				.execute();
		}

		return getById(normalizedId);
	}

	public static boolean remove(String id) {
		final String normalizedId = normalizeId(id);
		if (normalizedId == null || normalizedId.isBlank()) return false;
		PackOwnershipTable.deleteByPackId(normalizedId);
		final boolean deleted = tryDeletePackRow(normalizedId);
		if (deleted) {
			final File file = new File(Directories.BASE_DIR, "downloads/" + normalizedId + ".zip");
			if (file.exists()) file.delete();
		}
		return deleted;
	}

	private static boolean tryDeletePackRow(String packId) {
		try {
			PhotonEngine.DATA_BASE.connect();
			try (var statement = PhotonEngine.DATA_BASE.sqlConnection().createStatement()) {
				return statement.executeUpdate("DELETE FROM \"Packs\" WHERE id = '" + packId.replace("'", "''") + "'") > 0;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
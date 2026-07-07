package niwer.photon.sql;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPackProduct;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
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
			createColumn(db, "stripe_payment_link", EnumColumnTypes.TEXT),
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
		return querySingle("SELECT * FROM \"Packs\" WHERE id = ? LIMIT 1", normalizeId(id));
	}

	public static ObjectPackProduct getByStripePriceId(String priceId) {
		if (priceId == null || priceId.isBlank()) return null;
		return querySingle("SELECT * FROM \"Packs\" WHERE stripe_price_id = ? LIMIT 1", priceId.trim());
	}

	public static List<ObjectPackProduct> getAllActive() {
		return queryList("SELECT * FROM \"Packs\" WHERE status = 'ACTIVE'");
	}

	public static ObjectPackProduct upsertPack(String id, String name, String description, String category, String stripePriceId, String stripePaymentLink, String filePath, String versionNumber, String status) {
		final String normalizedId = normalizeId(id);
		final Date now = new Date();
		final ObjectPackProduct current = getById(normalizedId);
		final String nextFilePath = filePath == null || filePath.isBlank() ? "/downloads/" + normalizedId + ".zip" : filePath;
		final String nextVersion = versionNumber == null || versionNumber.isBlank() ? "1.0" : versionNumber;
		final String nextStatus = status == null || status.isBlank() ? "ACTIVE" : status;

		if (current == null) {
			InsertionManager.insert(PhotonEngine.DATA_BASE, PackProductTable.class, "id", "name", "description", "category", "stripe_price_id", "stripe_payment_link", "file_path", "version_number", "status", "created_at", "updated_at")
				.row(normalizedId, name, description, category, stripePriceId, stripePaymentLink, nextFilePath, nextVersion, nextStatus, now, now)
				.execute();
		} else {
			UpdateManager.update(PhotonEngine.DATA_BASE, PackProductTable.class)
				.set("name", name)
				.set("description", description)
				.set("category", category)
				.set("stripe_price_id", stripePriceId)
				.set("stripe_payment_link", stripePaymentLink)
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

	private static List<ObjectPackProduct> queryList(String sql) {
		final List<ObjectPackProduct> packs = new ArrayList<>();
		try {
			PhotonEngine.DATA_BASE.connect();
			try (Statement statement = PhotonEngine.DATA_BASE.sqlConnection().createStatement(); ResultSet result = statement.executeQuery(sql)) {
				while (result.next()) packs.add(fromResultSet(result));
			}
		} catch (Exception ignored) {}
		return packs;
	}

	private static ObjectPackProduct querySingle(String sql, String parameter) {
		final String escaped = parameter == null ? null : parameter.replace("'", "''");
		return queryList(sql.replace("?", "'" + escaped + "'" )).stream().findFirst().orElse(null);
	}

	private static ObjectPackProduct fromResultSet(ResultSet result) throws Exception {
		return new ObjectPackProduct(
			result.getString("id"),
			result.getString("name"),
			result.getString("description"),
			result.getString("category"),
			result.getString("stripe_price_id"),
			result.getString("stripe_payment_link"),
			result.getString("file_path"),
			result.getString("version_number"),
			result.getString("status"),
			result.getTimestamp("created_at"),
			result.getTimestamp("updated_at")
		);
	}
}
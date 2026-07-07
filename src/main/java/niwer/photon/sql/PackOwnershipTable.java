package niwer.photon.sql;

import java.util.Date;
import java.util.List;

import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPackOwnership;
import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class PackOwnershipTable extends Table {

	public PackOwnershipTable(DataBase db) {
		super(db);

		this.addColumns(
			createColumn(db, "user_email", EnumColumnTypes.TEXT).notNull(),
			createColumn(db, "account_uuid", EnumColumnTypes.TEXT),
			createColumn(db, "pack_id", EnumColumnTypes.TEXT).notNull(),
			createColumn(db, "purchased_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP"),
			createColumn(db, "first_download_at", EnumColumnTypes.DATE_TIME),
			createColumn(db, "is_active", EnumColumnTypes.BOOLEAN).defaultValue(true),
			createColumn(db, "claimed_successfully", EnumColumnTypes.BOOLEAN).defaultValue(false),
			createColumn(db, "updated_at", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP")
		).execute();
	}

	@Override public String name() { return "PackOwnership"; }

	public static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	public static ObjectPackOwnership getByEmailAndPackId(String email, String packId) {
		if (email == null || email.isBlank() || packId == null || packId.isBlank()) return null;
		return SelectionManager.select(PhotonEngine.DATA_BASE, PackOwnershipTable.class)
			.where(Expression.of("user_email").isEqualTo(normalizeEmail(email)).and(Expression.of("pack_id").isEqualTo(packId.trim())))
			.limit(1)
			.executeSerializable(ObjectPackOwnership.class);
	}

	public static List<ObjectPackOwnership> getByEmail(String email) {
		if (email == null || email.isBlank()) return List.of();
		return SelectionManager.select(PhotonEngine.DATA_BASE, PackOwnershipTable.class)
			.where(Expression.of("user_email").isEqualTo(normalizeEmail(email)))
			.executeList(ObjectPackOwnership.class);
	}

	public static ObjectPackOwnership upsertOwnership(String email, String accountUuid, String packId, boolean active) {
		final String normalizedEmail = normalizeEmail(email);
		final String normalizedPackId = packId == null ? null : packId.trim();
		final Date now = new Date();
		final ObjectPackOwnership current = getByEmailAndPackId(normalizedEmail, normalizedPackId);
		if (current == null) {
			InsertionManager.insert(PhotonEngine.DATA_BASE, PackOwnershipTable.class, "user_email", "account_uuid", "pack_id", "purchased_at", "first_download_at", "is_active", "claimed_successfully", "updated_at")
				.row(normalizedEmail, accountUuid, normalizedPackId, now, null, active, false, now)
				.execute();
		} else {
			UpdateManager.update(PhotonEngine.DATA_BASE, PackOwnershipTable.class)
				.set("account_uuid", accountUuid)
				.set("is_active", active)
				.set("updated_at", now)
				.where(Expression.of("user_email").isEqualTo(normalizedEmail).and(Expression.of("pack_id").isEqualTo(normalizedPackId)))
				.execute();
		}
		return getByEmailAndPackId(normalizedEmail, normalizedPackId);
	}

	public static boolean markFirstDownload(String email, String accountUuid, String packId) {
		final ObjectPackOwnership current = getByEmailAndPackId(email, packId);
		if (current == null) return false;
		final Date now = new Date();
		UpdateManager.update(PhotonEngine.DATA_BASE, PackOwnershipTable.class)
			.set("account_uuid", accountUuid)
			.set("first_download_at", current.firstDownloadAt() == null ? now : current.firstDownloadAt())
			.set("claimed_successfully", true)
			.set("updated_at", now)
			.where(Expression.of("user_email").isEqualTo(normalizeEmail(email)).and(Expression.of("pack_id").isEqualTo(packId.trim())))
			.execute();
		return true;
	}

	public static boolean owns(String email, String accountUuid, String packId) {
		if (packId == null || packId.isBlank()) return false;
		final ObjectPackOwnership byEmail = getByEmailAndPackId(email, packId);
		if (byEmail != null && Boolean.TRUE.equals(byEmail.isActive())) return true;
		if (accountUuid == null || accountUuid.isBlank()) return false;
		return SelectionManager.select(PhotonEngine.DATA_BASE, PackOwnershipTable.class)
			.where(Expression.of("account_uuid").isEqualTo(accountUuid.trim()).and(Expression.of("pack_id").isEqualTo(packId.trim())).and(Expression.of("is_active").isEqualTo(true)))
			.executeHasResult();
	}

	public static boolean deleteByPackId(String packId) {
		if (packId == null || packId.isBlank()) return false;
		try {
			PhotonEngine.DATA_BASE.connect();
			try (var statement = PhotonEngine.DATA_BASE.sqlConnection().createStatement()) {
				statement.executeUpdate("DELETE FROM \"PackOwnership\" WHERE pack_id = '" + packId.trim().replace("'", "''") + "'");
				return true;
			}
		} catch (Exception ignored) {
			return false;
		}
	}
}
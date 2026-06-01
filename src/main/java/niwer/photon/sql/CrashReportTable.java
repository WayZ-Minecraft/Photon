package niwer.photon.sql;

import niwer.photon.PhotonEngine;
import niwer.photon.util.TestHooks;
import niwer.queryon.DataBase;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class CrashReportTable extends Table {

    public CrashReportTable(DataBase db) {
        super(db);

        this.addColumns(
            createColumn(db, "id", EnumColumnTypes.INT).primaryKey(),
            createColumn(db, "userUUID", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "fileName", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "fileMessage", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "side", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "timestamp", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP")
        ).execute();
    }

    @Override public String name() { return "CrashReport"; }

    /**
     * Save a crash report to the database.
     * 
     * @param userUUID The user UUID
     * @param fileName The file name
     * @param fileMessage The crash report message
     * @param side The side of the crash report
     */
    public static void save(String userUUID, String fileName, String fileMessage, CrashReportSides side) {
        if (TestHooks.invokeStaticVoid("niwer.photon.sql.tables.CrashReportTableTest", "save", new Class<?>[] { String.class, String.class, String.class, String.class }, userUUID, fileName, fileMessage, side.name())) {
            return;
        }

        InsertionManager.insert(PhotonEngine.DATA_BASE, CrashReportTable.class, "userUUID", "fileName", "fileMessage", "side")
            .row(userUUID, fileName, fileMessage, side.name())
            .execute();
    }

    public static enum CrashReportSides {
        CLIENT, SERVER;

        public static CrashReportSides fromString(String value) {
            for (CrashReportSides side : values()) {
                if (side.name().equalsIgnoreCase(value)) return side;
            }
            throw new IllegalArgumentException("Invalid crash report side: " + value);
        }
    }
}
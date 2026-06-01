package niwer.photon.sql;

import niwer.photon.PhotonEngine;
import niwer.photon.util.TestHooks;
import niwer.queryon.DataBase;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

public class AnticheatTable extends Table {

    public AnticheatTable(DataBase db) {
        super(db);

        this.addColumns(
            createColumn(db, "id", EnumColumnTypes.INT).primaryKey(),
            createColumn(db, "userUUID", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "fileName", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "fileMessage", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "operatingSystem", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "timestamp", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP")
        ).execute();
    }

    @Override public String name() { return "Anticheat"; }

    /**
     * @param userUUID The user UUID
     * @param fileMessage The cheat detection message
     * @param operatingSystem The operating system
     */
    public static void save(String userUUID, String fileName, String fileMessage, String operatingSystem) {
        if (TestHooks.invokeStaticVoid("niwer.photon.sql.tables.AnticheatTableTest", "save", new Class<?>[] { String.class, String.class, String.class, String.class }, userUUID, fileName, fileMessage, operatingSystem)) {
            return;
        }

        InsertionManager.insert(PhotonEngine.DATA_BASE, AnticheatTable.class, "userUUID", "fileName", "fileMessage", "operatingSystem")
            .row(userUUID, fileName, fileMessage, operatingSystem)
            .execute();
    }
}
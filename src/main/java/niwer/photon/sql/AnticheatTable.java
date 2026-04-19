package niwer.photon.sql;

import niwer.photon.PhotonEngine;

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
            createColumn(db, "fileName", 2048).notNull(),
            createColumn(db, "fileMessage", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "operatingSystem", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "timestamp", EnumColumnTypes.DATE_TIME).defaultValue("CURRENT_TIMESTAMP")
        ).execute();
    }

    @Override public String name() { return "Anticheat"; }

    /**
     * @param userUUID The user UUID
     * @param fileName The file name
     * @param fileMessage The cheat detection message
     * @param operatingSystem The operating system
     */
    public static void save(String userUUID, String fileName, String fileMessage, String operatingSystem) {
        InsertionManager.insert(PhotonEngine.DATA_BASE, AnticheatTable.class, "userUUID", "fileName", "fileMessage", "operatingSystem")
            .row(userUUID, fileName, fileMessage, operatingSystem)
            .execute();
    }
}
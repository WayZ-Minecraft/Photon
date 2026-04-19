package com.photon.sql;

import com.photon.PhotonEngine;
import com.photon.util.NetworkOnly;

import niwer.queryon.DataBase;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.tables.EnumColumnTypes;
import niwer.queryon.tables.Table;

@NetworkOnly
public class CrashReportTable extends Table {

    public CrashReportTable(DataBase db) {
        super(db);

        this.addColumns(
            createColumn(db, "id", EnumColumnTypes.INT).primaryKey(),
            createColumn(db, "userUUID", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "fileName", EnumColumnTypes.TEXT).notNull(),
            createColumn(db, "fileMessage", EnumColumnTypes.TEXT).notNull(),
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
     */
    public static void save(String userUUID, String fileName, String fileMessage) {
        InsertionManager.insert(PhotonEngine.DATA_BASE, CrashReportTable.class, "userUUID", "fileName", "fileMessage")
            .row(userUUID, fileName, fileMessage)
            .execute();
    }
}
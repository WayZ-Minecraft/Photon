package com.photon.sql;

import java.sql.Date;
import java.sql.SQLException;

import com.photon.network.NetworkEngine;
import com.photon.network.objects.ObjectNews;
import com.photon.util.NetworkOnly;
import com.photon.util.TranslationManager.Language;

import niwer.queryon.DataBase;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.tables.Table;

@NetworkOnly
public class NewsTable extends Table {

    public NewsTable(DataBase db) {
        super(db);
        this.addColumnsFromClass(ObjectNews.class)
            .execute();
    }

    @Override public String name() { return "News"; }

    /**
     * Save a news entry to the database.
     * 
     * @param news The ObjectNews to save
     * @throws SQLException if query execution fails
     */
    public static void createNews(ObjectNews news) throws SQLException {
        InsertionManager.insert(NetworkEngine.DATA_BASE, NewsTable.class, "title", "contentEn", "contentFr", "date", "imagepath")
            .row(news.title(), news.contentForLang(Language.ENGLISH), news.contentForLang(Language.FRENCH), new Date(news.date().getTime()), news.imageURL())
            .execute();
    }
}
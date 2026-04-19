package com.photon.sql;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.photon.network.NetworkEngine;
import com.photon.objects.ObjectNews;
import com.photon.util.NetworkOnly;
import com.photon.util.TranslationManager.Language;

import niwer.queryon.DataBase;
import niwer.queryon.QueryonException;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
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

    /**
     * Retrieve all news entries from the database.
     * 
     * @return A list of ObjectNews representing all news entries in the database
     */
    public static List<ObjectNews> getAllNews() {
        try {
            return SelectionManager.select(NetworkEngine.DATA_BASE,NewsTable.class).executeList(ObjectNews.class);
        } catch (QueryonException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
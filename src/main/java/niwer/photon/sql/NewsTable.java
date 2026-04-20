package niwer.photon.sql;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectNews;
import niwer.photon.util.TranslationManager.Language;

import niwer.queryon.DataBase;
import niwer.queryon.QueryonException;
import niwer.queryon.queries.Expression;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;
import niwer.queryon.queries.interaction.UpdateManager;
import niwer.queryon.queries.interaction.DeletionManager;
import niwer.queryon.tables.Table;

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
        InsertionManager.insert(PhotonEngine.DATA_BASE, NewsTable.class, "title", "contentEn", "contentFr", "date", "image")
            .row(news.title(), news.contentForLang(Language.ENGLISH), news.contentForLang(Language.FRENCH), new Date(news.date().getTime()), news.imageURL())
            .execute();
    }

    public static ObjectNews getById(int id) {
        try {
            return SelectionManager.select(PhotonEngine.DATA_BASE, NewsTable.class)
                .where(Expression.of("id").isEqualTo(id))
                .limit(1)
                .executeSerializable(ObjectNews.class);
        } catch (QueryonException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void updateNews(ObjectNews news) throws SQLException {
        if (news == null) return;

        UpdateManager.update(PhotonEngine.DATA_BASE, NewsTable.class)
            .set("title", news.title())
            .set("contentEn", news.contentForLang(Language.ENGLISH))
            .set("contentFr", news.contentForLang(Language.FRENCH))
            .set("date", new Date(news.date().getTime()))
            .set("image", news.imageURL())
            .where(Expression.of("id").isEqualTo(news.id()))
            .execute();
    }

    public static void deleteNews(int id) {
        DeletionManager.delete(PhotonEngine.DATA_BASE, NewsTable.class)
            .where(Expression.of("id").isEqualTo(id))
            .execute();
    }

    /**
     * Retrieve all news entries from the database.
     * 
     * @return A list of ObjectNews representing all news entries in the database
     */
    public static List<ObjectNews> getAllNews() {
        try {
            return SelectionManager.select(PhotonEngine.DATA_BASE,NewsTable.class).executeList(ObjectNews.class);
        } catch (QueryonException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
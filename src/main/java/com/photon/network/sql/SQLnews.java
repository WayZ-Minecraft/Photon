package com.photon.network.sql;

import java.sql.Date;
import java.sql.SQLException;

import com.photon.network.objects.ObjectNews;
import com.photon.util.NetworkOnly;

@NetworkOnly
public class SQLnews extends SQLInteraction{

    /**
     * Create News table with columns: id, title, contentEn, contentFr, date, imagepath.
     */
    @Override
    public void register() {
        executeSQLCommand("CREATE TABLE IF NOT EXISTS News (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, contentEn TEXT, contentFr TEXT, date DATE NOT NULL, image TEXT);");
    }

    /**
     * Save a news entry to the database.
     * 
     * @param news The ObjectNews to save
     * @throws SQLException if query execution fails
     */
    public static void createNews(ObjectNews news) throws SQLException {
        final String title = news.getTitle();
        final String contentEn = news.getContent("en");
        final String contentFr = news.getContent("fr");
        final Date date = new Date(news.getDate().getTime());
        final String imageUrl = news.getImageUrl();

        executeSQLCommand("INSERT INTO News (title, contentEn, contentFr, date, imagepath) VALUES (?, ?, ?, ?, ?)",
            title, contentEn, contentFr, date, imageUrl
        );
    }
}
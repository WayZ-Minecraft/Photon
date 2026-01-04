package com.photon.network.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.photon.network.objects.ObjectNews;

public class SQLnews extends SqlInteract{

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

        PreparedStatement statement = null;
        
        try {
            statement = connexion.prepareStatement(
                "INSERT INTO News (title, contentEn, contentFr, date, imagepath) VALUES (?, ?, ?, ?, ?)");
            
            statement.setString(1, title);
            statement.setString(2, contentEn);
            statement.setString(3, contentFr);
            statement.setDate(4, date);
            statement.setString(5, imageUrl);
            
            statement.executeUpdate();
        } finally {
            closeStatement(statement, null);
        }
    }
}
package com.photon.network.sql;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

import com.photon.network.objects.ObjectNews;

public class SQLnews extends SqlInteract{

    /**
     * Save a news in the database
     * @param ObjectNews the news to save
     * 
     * @throws SQLException
     */
    public static void createNews(ObjectNews news) throws SQLException {

        final String title = news.getTitle().replace("'", "''");
        final String contentEn = news.getContent("en").replace("'", "''");
        final String contentFr = news.getContent("fr").replace("'", "''");
        final Date date = new Date(news.getDate().getTime());
        final String imageUrl = news.getImageUrl();

        Statement statement = null;
        
        statement = connexion.createStatement();

        // Exécution de la requête
        String sqlUpdate = "INSERT INTO News (title, contentEn, contentFr, date, imagepath) VALUES ('"+title+"', '"+contentEn+"', '"+contentFr+"', '"+date+"', '"+imageUrl+"')";
        statement.executeUpdate(sqlUpdate);


        

        closeStatement(statement, null);

   
    }
}
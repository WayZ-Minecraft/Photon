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
        final String content = news.getContent().replace("'", "''");
        final Date date = news.getDate();
        final String imageUrl = news.getImageUrl();

        Statement statement = null;
        
        statement = connexion.createStatement();

        // Exécution de la requête
        String sqlUpdate = "INSERT INTO News (title, content, date, imagepath) VALUES ('"+title+"', '"+content+"', '"+date+"', '"+imageUrl+"')";
        statement.executeUpdate(sqlUpdate);


        

        closeStatement(statement, null);

   
    }
}

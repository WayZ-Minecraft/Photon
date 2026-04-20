package niwer.photon.web.endpoints.admin;

import java.sql.SQLException;
import java.util.Date;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectNews;
import niwer.photon.sql.NewsTable;
import niwer.photon.util.TranslationManager.Language;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminNewsCreateEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/news"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        if (AdminSessionManager.requireProjectAuthor(handler) == null) return;

        final ObjectNews news;
        try {
            news = Directories.GSON.fromJson(handler.body(), ObjectNews.class);
        } catch (Exception e) {
            handler.status(400).result("Invalid news payload");
            return;
        }

        if (news == null || news.title() == null || news.title().isBlank()) {
            handler.status(400).result("News title is required");
            return;
        }

        final ObjectNews payload = new ObjectNews(
            news.title(),
            news.contentForLang(Language.ENGLISH),
            news.contentForLang(Language.FRENCH),
            news.date() == null ? new Date() : news.date(),
            news.imageURL()
        );

        try {
            NewsTable.createNews(payload);
            handler.json(payload);
        } catch (SQLException e) {
            handler.status(500).result("Failed to create news: " + e.getMessage());
        }
    }
}
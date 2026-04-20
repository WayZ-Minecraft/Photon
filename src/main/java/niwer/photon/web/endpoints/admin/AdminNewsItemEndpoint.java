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

public class AdminNewsItemEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/news/{id}"; }

    @Override public HttpMethod method() { return HttpMethod.PUT; }

    @Override
    public void handle(Context handler) {
        if (AdminSessionManager.requireProjectAuthor(handler) == null) return;

        final int id;
        try {
            id = Integer.parseInt(handler.pathParam("id"));
        } catch (NumberFormatException e) {
            handler.status(400).result("Invalid news id");
            return;
        }

        final ObjectNews existing = NewsTable.getById(id);
        if (existing == null) {
            handler.status(404).result("News entry not found");
            return;
        }

        final ObjectNews incoming;
        try {
            incoming = Directories.GSON.fromJson(handler.body(), ObjectNews.class);
        } catch (Exception e) {
            handler.status(400).result("Invalid news payload");
            return;
        }

        if (incoming == null || incoming.title() == null || incoming.title().isBlank()) {
            handler.status(400).result("News title is required");
            return;
        }

        final ObjectNews updated = new ObjectNews(
            id,
            incoming.title(),
            incoming.contentForLang(Language.ENGLISH),
            incoming.contentForLang(Language.FRENCH),
            incoming.date() == null ? new Date(existing.date().getTime()) : incoming.date(),
            incoming.imageURL()
        );

        try {
            NewsTable.updateNews(updated);
            handler.json(updated);
        } catch (SQLException e) {
            handler.status(500).result("Failed to update news: " + e.getMessage());
        }
    }
}
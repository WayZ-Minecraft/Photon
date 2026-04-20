package niwer.photon.web.endpoints.admin;

import io.javalin.http.Context;
import niwer.photon.sql.NewsTable;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminNewsDeleteEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/news/{id}"; }

    @Override public HttpMethod method() { return HttpMethod.DELETE; }

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

        NewsTable.deleteNews(id);
        handler.status(200).result("Deleted");
    }
}
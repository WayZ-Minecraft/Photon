package niwer.photon.web.endpoints.news;

import niwer.photon.sql.NewsTable;
import niwer.photon.web.endpoints.IEndpoint;

import io.javalin.http.Context;

public class NewsListEndpoint implements IEndpoint {

    @Override public String path() { return "/api/news"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        handler.json(NewsTable.getAllNews());
    }
}
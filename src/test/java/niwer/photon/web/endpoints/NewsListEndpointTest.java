package niwer.photon.web.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.objects.ObjectNews;
import niwer.photon.sql.NewsTable;

class NewsListEndpointTest {

    @AfterEach
    void resetState() {
        NewsTable.reset();
    }

    @Test
    void exposesTheExpectedPathAndMethod() {
        final var endpoint = new niwer.photon.web.endpoints.news.NewsListEndpoint();

        assertEquals("/api/news", endpoint.path());
        assertEquals(IEndpoint.HttpMethod.GET, endpoint.method());
    }

    @Test
    void returnsAllNewsAsJson() {
        final List<ObjectNews> news = List.of(
            new ObjectNews(1, "Title 1", "English 1", "French 1", new Date(0L), "https://example.com/1.png"),
            new ObjectNews(2, "Title 2", "English 2", "French 2", new Date(1_000L), "https://example.com/2.png")
        );
        NewsTable.setAllNews(news);

        final ContextStub stub = new ContextStub();
        new niwer.photon.web.endpoints.news.NewsListEndpoint().handle(stub.context());

        assertEquals(news, stub.jsonBody());
    }
}
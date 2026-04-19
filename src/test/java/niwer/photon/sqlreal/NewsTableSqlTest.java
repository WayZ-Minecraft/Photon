package niwer.photon.sqlreal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.objects.ObjectNews;
import niwer.queryon.QueryonException;
import niwer.queryon.queries.interaction.InsertionManager;
import niwer.queryon.queries.interaction.SelectionManager;

class NewsTableSqlTest {

    private static final String CLASS_NAME = "niwer.photon.sql.NewsTable";

    @AfterEach
    void resetState() {
        SelectionManager.reset();
        InsertionManager.reset();
    }

    @Test
    void createNewsStoresTheTranslatedContentAndImagePath() throws Exception {
        final ObjectNews news = new ObjectNews(1, "Title", "English", "French", new Date(123_000L), "https://example.com/image.png");

        SqlProductionTestSupport.invokeStatic(CLASS_NAME, "createNews", new Class<?>[] { ObjectNews.class }, news);

        final Object[] row = InsertionManager.lastCall().rows().get(0);
        assertEquals("Title", row[0]);
        assertEquals("English", row[1]);
        assertEquals("French", row[2]);
        assertEquals(new java.sql.Date(123_000L), row[3]);
        assertEquals("https://example.com/image.png", row[4]);
    }

    @Test
    void getAllNewsReturnsTheConfiguredListOrEmptyOnFailure() throws Exception {
        final ObjectNews news = new ObjectNews(1, "Title", "English", "French", new Date(123_000L), "https://example.com/image.png");
        SelectionManager.setNextListResult(List.of(news));
        assertEquals(List.of(news), SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAllNews", new Class<?>[0]));

        SelectionManager.setNextFailure(new QueryonException("boom"));
        final List<?> result = (List<?>) SqlProductionTestSupport.invokeStatic(CLASS_NAME, "getAllNews", new Class<?>[0]);
        assertTrue(result.isEmpty());
    }
}
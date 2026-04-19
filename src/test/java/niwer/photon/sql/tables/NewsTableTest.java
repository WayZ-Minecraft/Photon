package niwer.photon.sql.tables;

import java.util.ArrayList;
import java.util.List;

import niwer.photon.objects.ObjectNews;

public final class NewsTableTest {

    private static List<ObjectNews> allNews = new ArrayList<>();

    private NewsTableTest() {}

    public static void reset() {
        allNews = new ArrayList<>();
    }

    public static void setAllNews(List<ObjectNews> news) {
        allNews = new ArrayList<>(news);
    }

    public static List<ObjectNews> getAllNews() {
        return allNews;
    }
}

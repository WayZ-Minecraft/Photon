package niwer.photon.sql;

import java.util.ArrayList;
import java.util.List;

import niwer.photon.objects.ObjectNews;

public final class NewsTable {

    private static List<ObjectNews> allNews = new ArrayList<>();

    private NewsTable() {}

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
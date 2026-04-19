package niwer.photon.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.Test;

import niwer.photon.util.TranslationManager.Language;

class ObjectNewsTest {

    @Test
    void contentForLangSelectsTheCorrectLanguage() {
        final ObjectNews news = new ObjectNews(7, "Title", "English body", "Corps français", new Date(0L), "https://example.com/news.png");

        assertEquals("English body", news.contentForLang(Language.ENGLISH));
        assertEquals("Corps français", news.contentForLang(Language.FRENCH));
    }

    @Test
    void discordEmbedCopiesTheNewsFields() {
        final Date date = new Date(0L);
        final ObjectNews news = new ObjectNews(7, "Photon update", "English content", "Contenu français", date, "https://example.com/news.png");

        final var embed = news.discordEmbed().build();

        assertEquals("Photon update", embed.getTitle());
        assertNotNull(embed.getDescription());
        assertTrue(embed.getDescription().contains("🇬🇧English content"));
        assertTrue(embed.getDescription().contains("🇫🇷Contenu français"));
        assertNotNull(embed.getImage());
        assertEquals("https://example.com/news.png", embed.getImage().getUrl());
        assertEquals(OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC), embed.getTimestamp());
    }

    @Test
    void accessorsExposeTheStoredValues() {
        final Date date = new Date(123_456L);
        final ObjectNews news = new ObjectNews(99, "Title", "English body", "Corps français", date, "https://example.com/banner.png");

        assertEquals(99, news.id());
        assertEquals("Title", news.title());
        assertEquals(date, news.date());
        assertEquals("https://example.com/banner.png", news.imageURL());
    }
}
package niwer.photon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import niwer.photon.util.TranslationManager.Language;

class TranslationManagerTest {

    @AfterEach
    void clearTranslations() throws Exception {
        translations().clear();
    }

    @Test
    void formatsTranslationsAndFallsBackToEnglish() throws Exception {
        translations().put(Language.ENGLISH, Map.of(
            "greeting", "Hello %s",
            "only.english", "English only"
        ));
        translations().put(Language.FRENCH, Map.of(
            "greeting", "Bonjour %s"
        ));

        assertEquals("Hello Photon", TranslationManager.format(Language.ENGLISH, "greeting", "Photon"));
        assertEquals("Bonjour Photon", TranslationManager.format(Language.FRENCH, "greeting", "Photon"));
        assertEquals("English only", TranslationManager.format(Language.GERMAN, "only.english"));
        assertEquals("missing.key", TranslationManager.format(Language.ENGLISH, "missing.key"));
    }

    @Test
    void languageEnumResolvesCodeAndNameStrings() {
        assertEquals(Language.FRENCH, Language.fromCodeString("fr"));
        assertEquals(Language.RUSSIAN, Language.fromNameString("russian"));
        assertEquals(Language.ENGLISH, Language.fromCodeString("zz"));
        assertEquals(Language.ENGLISH, Language.fromNameString("not-a-language"));
        assertEquals("en", Language.ENGLISH.code());
        assertEquals("fr", Language.FRENCH.code());
        assertEquals(java.util.Locale.of("de", "DE"), Language.GERMAN.locale());
    }

    @Test
    void loadAllLanguagesReadsBundledResources() {
        assertDoesNotThrow(() -> TranslationManager.loadAllLanguages("lang"));
        assertEquals("You can edit a message that contains a link.", TranslationManager.format(Language.ENGLISH, "discord.securityMessage.updateMessage"));
        assertEquals("Vous ne pouvez pas modifier un message contenant un lien.", TranslationManager.format(Language.FRENCH, "discord.securityMessage.updateMessage"));
    }

    private static Map<Language, Map<String, String>> translations() throws Exception {
        final Field field = TranslationManager.class.getDeclaredField("TRANSLATIONS");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        final Map<Language, Map<String, String>> translations = (Map<Language, Map<String, String>>) field.get(null);
        return translations;
    }
}
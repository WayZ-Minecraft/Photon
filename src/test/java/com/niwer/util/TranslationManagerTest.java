package com.niwer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import niwer.photon.util.TranslationManager.Language;

public class TranslationManagerTest {

    @Test
    public void testLanguageAcceptsStoredNameAndCode() {
        assertEquals(Language.ENGLISH, Language.fromString("ENGLISH"));
        assertEquals(Language.ENGLISH, Language.fromString("en"));
        assertEquals(Language.FRENCH, Language.fromString("fr"));
    }
}

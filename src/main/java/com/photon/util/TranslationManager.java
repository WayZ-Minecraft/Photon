package com.photon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.photon.util.os.FileLocation;

@NetworkOnly
public class TranslationManager {

	/* Variables that represent avalible locales */
	public static final Locale locale_en = new Locale("en", "US"); 
	public static final Locale locale_fr = new Locale("fr", "FR");
	public static final Locale locale_de = new Locale("de", "DE");
	public static final Locale locale_ru = new Locale("ru", "RU");

	public static String activeLocale = "en"; // For Launcher

	/* The list that contains every locales */
	private static ArrayList<Locale> locales = new ArrayList<>(){{
		add(locale_en);
		add(locale_fr);
		add(locale_de);
		add(locale_ru);
	}};

	/* The list that saves the languages and properties for these languages */
	private static final HashMap<String, HashMap<String, String>> languages = new HashMap<>();
	

	/**
	 * Load all languages from the given path.
	 * @param path The path to load from.
	 */
	public static void loadAllLanguages(String path) { locales.forEach(locale -> load(locale, path)); }
	
	/** 
	 * Load a language from the given path.
	 * @param locale The locale to load as string (e.g en, fr, ru, ...).
	 * @param path The path to load from.
	 */
	public static void load(String localeFromString, String path) {
		Locale locale = locale_en;
		if(localeFromString !=null) {
			switch(localeFromString.toLowerCase()) {
				case "en": locale = locale_en; break;
				case "fr": locale = locale_fr; break;
				case "de": locale = locale_de; break;
				case "ru": locale = locale_ru; break;
			}
		}
		load(locale, path);
		activeLocale = locale.getLanguage();
	}
	

	/** 
	 * Load a language from the given path.
	 * @param locale The locale to load. (e.g locale_en, locale_fr, locale_ru, ...)
	 * @param path The path to load from.
	 * @see TranslationManager#load(String, String)
	 */
	private static void load(Locale locale, String path) {
		try {
			/* Create a list to save the properties (keys and value) for this locale */
			final HashMap<String, String> properties = new HashMap<String, String>();
			
			/* Get the file as InputStream from locale */
			final InputStream stream = FileLocation.loadFile(path + "/lang_" + locale.getLanguage() + ".properties");
			if(stream == null) return;

			/* Read the file */

			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String line;
			while((line = reader.readLine()) !=null) {
				/* If the line is not empty and not a comment, add it to the list */
				if(!line.isEmpty() && line.charAt(0) != '#' && (line.charAt(0) != '/' && line.charAt(1) != '/')) {
					properties.put(
						/* Get the line from 0 to the first '=' */
						line.substring(0, line.indexOf("=")),
						/* Get the line from '=' to the end */
						line.substring(line.indexOf("=") + 1)
						);
					}
				}
			/* Close everything ! :) */
			stream.close();
			reader.close();
			
			/* Added the properties to then required language */
			languages.put(locale.getLanguage(), properties);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the translation for the given key.
	 * @param locale The language to get the translation for. (e.g en, fr, ru, ...)
	 * @param key The key to get the translation for. (e.g "button.title")
	 * @param obj The objects to format the translation with. (The system replace every %s with the given object in order)
	 * @return The translation for the given key.
	 */	
	public static String format(String locale, String key, Object... obj) {
		/* Get the properties for the choosen lang */
		final HashMap<String, String> properties = TranslationManager.languages.get(locale);

		/* Get the propertie for the current key */

		final String prop = properties.get(key);

		/* Return the Translated key */
		return prop == null ? key : String.format(prop, obj);
	}

	public static String format(String locale, String key) { return format(locale, key, new Object()); }

	public static String format(String key, Object... obj) { return format(activeLocale, key, obj); }
	
	public static String format(String key) { return format(activeLocale, key, new Object()); }


    /**
     * Get the system language in locale format
     * @return the lowercase language code (ex: en, fr, de, ru)
     * @autor Created by Niwer
     */
	public static String getSystem() {
		String lang = System.getProperty("user.language");
		switch(lang.toLowerCase()) {
			case "en": return locale_en.getLanguage();
			case "fr": return locale_fr.getLanguage();
			case "de": return locale_de.getLanguage();
			case "ru": return locale_ru.getLanguage();
			default: return locale_en.getLanguage();
		}
	}
}

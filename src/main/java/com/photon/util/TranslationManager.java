package com.photon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import com.photon.util.os.FileLocation;

public class TranslationManager {
	
	public static final Locale locale_en = new Locale("en", "US"); 
	public static final Locale locale_fr = new Locale("fr", "FR");
	public static final Locale locale_de = new Locale("de", "DE");
	public static final Locale locale_ru = new Locale("ru", "RU");
	private static final HashMap<String, String> properties = new HashMap<String, String>();
	
    /**
     * Load the language file
     * @param locale : The language code in string format
     * @param path : The path of the language file
     * @autor Created by Niwer
     */
	public static void load(String localeFromString, String path) {
		Locale locale = locale_en;
		if(localeFromString !=null) {
			if(localeFromString.equalsIgnoreCase(locale_en.getLanguage())) locale = locale_en;
			else if(localeFromString.equalsIgnoreCase(locale_fr.getLanguage())) locale = locale_fr;
		}
		load(locale, path);
	}
	
    /**
     * Load the language file
     * @param locale : The language code in locale format
     * @param path : The path of the language file
     * @autor Created by Niwer
     */
	public static void load(Locale locale, String path) {
		try {
			InputStream stream = FileLocation.loadFile(path + "/lang_" + locale.getLanguage() + ".properties");
			if(stream == null) return;

			/* Read the file */
			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String line;
			properties.clear();
			while((line = reader.readLine()) !=null) {
				if(!line.isEmpty() && line.charAt(0) != '#' && (line.charAt(0) != '/' && line.charAt(1) != '/')) {
					String key = line.substring(0, line.indexOf("="));
					String translation = line.substring(line.indexOf("=") + 1);
					properties.put(key, translation);
				}
			}
			stream.close();
			reader.close();
		} catch (IOException e) {}
	}
	
    /**
     * Format a text for specific language
     * @param key : the key of the translation
     * @return the translation
     * @autor Created by Niwer
     */
	public static String format(String key) { return format(key, new Object()); }
	
    /**
     * Format a text for specific language with objects
     * @param key : the key of the translation
     * @param obj : the object(s) to format the translation
     * @return the translation with the formatted object(s)
     * @autor Created by Niwer
     */
	public static String format(String key, Object... obj) {
		final String prop = properties.get(key);
		return prop == null ? key : String.format(prop, obj);
	}

    /**
     * Get the system language in locale format
     * @return the lowercase language code (ex: en, fr, de, ru)
     * @autor Created by Niwer
     */
	public static String getSystem() {
		String lang = System.getProperty("user.language");
		if(lang.equalsIgnoreCase(locale_fr.getLanguage())) return locale_fr.getLanguage();
		else if(lang.equalsIgnoreCase(locale_de.getLanguage())) return locale_de.getLanguage();
		else if(lang.equalsIgnoreCase(locale_ru.getLanguage())) return locale_ru.getLanguage();
		return locale_en.getLanguage();
	}
}

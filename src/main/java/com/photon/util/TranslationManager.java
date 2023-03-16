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
	private static final HashMap<String, String> properties = new HashMap<String, String>();
	
	public static void load(String localeFromString, String path) {
		Locale locale = locale_en;
		if(localeFromString !=null) {
			if(localeFromString.equalsIgnoreCase(locale_en.getLanguage())) locale = locale_en;
			else if(localeFromString.equalsIgnoreCase(locale_fr.getLanguage())) locale = locale_fr;
		}
		load(locale, path);
	}
	
	public static void load(Locale locale, String path) {
		try {
			InputStream stream = FileLocation.loadFile(path + "/lang_" + locale.getLanguage() + ".properties");
			if(stream == null) return;
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
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
	
	public static String format(String key) { return format(key, new Object()); }
	
	public static String format(String key, Object... obj) {
		final String prop = properties.get(key);
		return prop == null ? key : String.format(prop, obj);
	}
}

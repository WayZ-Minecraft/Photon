package com.photon.network.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.photon.network.NetworkDirectories;

public class ProfileManager
{
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final SecureRandom random = new SecureRandom();
    
	public static Gson getGson() { return gson; }

    public static String getTokenFromEMail(final String name) {
    	final long longToken = Math.abs(random.nextLong());
    	return name + ":" + Long.toString(longToken, 16);
    }
    
	public static void deleteProfile(final ObjectPlayerAccount profile) {
		final File profileFile = new File(NetworkDirectories.profilesDirectory, profile.uuid + ".json");
		if (profileFile.exists()) profileFile.delete();
	}

    public static ArrayList<ObjectPlayerAccount> getAllPorifles() {
    	final ArrayList<ObjectPlayerAccount> list = new ArrayList<>();
    	try {
    		for(File file : NetworkDirectories.profilesDirectory.listFiles()) {
    			final BufferedReader br = new BufferedReader(new FileReader(file));
        		final ObjectPlayerAccount profile = ProfileManager.gson.<ObjectPlayerAccount>fromJson(br, ObjectPlayerAccount.class);
        		br.close();
        		list.add(profile);
    		}
    	} catch (IOException e) { e.printStackTrace(); }
        return list;
    }
    
    public static ObjectPlayerAccount getProfileFromEMail(final String email) {
    	for(ObjectPlayerAccount profile : getAllPorifles()) {
    		if(profile.email.equalsIgnoreCase(email)) return profile;
    	}
    	return null;
    }
    
    public static ObjectPlayerAccount getProfileFromDiscordID(final String discordID) {
    	for(ObjectPlayerAccount profile : getAllPorifles()) {
    		if(profile.discordID !=null && !profile.discordID.isEmpty() && profile.discordID.equals(discordID)) return profile;
    	}
    	return null;
    }
    
    public static ObjectPlayerAccount getProfileFromUUID(final String givenUUID) {
        try {
			if (doesProfileExistByUUID(givenUUID)) {
				final File profileFile = new File(NetworkDirectories.profilesDirectory, givenUUID + ".json");
        		final BufferedReader br = new BufferedReader(new FileReader(profileFile));
        		final ObjectPlayerAccount profile = ProfileManager.gson.<ObjectPlayerAccount>fromJson(br, ObjectPlayerAccount.class);
        		br.close();
        		return profile;
        	}
        } catch (IOException e) {}
        return null;
    }
    
	public static ObjectPlayerAccount getProfileFromUsername(final String username) {
		if(!doesProfileExistByUsername(username)) return null;
		for(ObjectPlayerAccount profile : getAllPorifles()) {
			if(profile.username.equalsIgnoreCase(username)) return profile;
		}
		return null;
	}

    public static ObjectPlayerAccount createPlayerProfile(final String username, final String email, final String password) {
    	try {
    		if(doesProfileExistByEMail(email)) return null;
    		if(doesProfileExistByUsername(username)) return null;
			if(username == null || email == null || password == null) return null;
			if(username.isEmpty() || email.isEmpty() || password.isEmpty()) return null;
    		final ObjectPlayerAccount profile = new ObjectPlayerAccount();
    		profile.email = email;
    		profile.username = username;
    		profile.password = password;
    		profile.uuid = UUID.randomUUID().toString().replace("-", "");
    		final File profileFile = new File(NetworkDirectories.profilesDirectory, profile.uuid + ".json");
    		if (!profileFile.exists()) {
    			profileFile.createNewFile();
    			final Writer writer = new FileWriter(profileFile);
				writer.write(ProfileManager.gson.toJson(profile));
				writer.close();
				return profile;
    		}
    	} catch (IOException e) {}
    	return null;
    }
    
    public static boolean doesProfileExistByEMail(final String email) {
    	for(ObjectPlayerAccount profile : getAllPorifles()) {
    		if(profile.email.equals(email)) { return true; }
    	}
    	return false;
    }
    
    public static boolean doesProfileExistByUsername(final String username) {
    	for(ObjectPlayerAccount profile : getAllPorifles()) {
    		if(profile.username.equalsIgnoreCase(username)) return true;
    	}
    	return false;
    }
    
    public static boolean doesProfileExistByUUID(final String givenUUID) {
        final File profileFile = new File(NetworkDirectories.profilesDirectory, givenUUID + ".json");
        return profileFile.exists() && profileFile.length() > 0L;
    }
    
	/**
	 * Checks if the profile exists for this UUID and if the auth code corresponds to the profile UUID
	 * @param givenUUID ingame UUID
	 * @param givenAuthCode auth code from ingame
	 * @return Boolean : true if the profile exists and the auth code corresponds to the profile UUID, false otherwise
	 */
    public static boolean isAuthCodeValid(final String givenUUID, final String givenAuthCode) {
        if (doesProfileExistByUUID(givenUUID)) { return getProfileFromUUID(givenUUID).discordAuthCode.equals(givenAuthCode); }
        return false;
    }
}

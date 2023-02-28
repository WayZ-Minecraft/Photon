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
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static SecureRandom random = new SecureRandom();
    
    public static String getTokenFromEMail(final String email) {
    	final long longToken = Math.abs(random.nextLong());
		final String random = Long.toString(longToken, 16);
    	return email + ":" + random;
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
    	} catch (IOException e) {}
        return list;
    }
    
    public static ObjectPlayerAccount getProfileFromEMail(final String email) {
    	for(ObjectPlayerAccount profile : getAllPorifles()) {
    		if(profile.email.equals(email)) return profile;
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
        	final File profileFile = new File(NetworkDirectories.profilesDirectory, givenUUID + ".json");
        	if (doesProfileExistByUUID(givenUUID)) {
        		final BufferedReader br = new BufferedReader(new FileReader(profileFile));
        		final ObjectPlayerAccount profile = ProfileManager.gson.<ObjectPlayerAccount>fromJson(br, ObjectPlayerAccount.class);
        		br.close();
        		return profile;
        	}
        } catch (IOException e) {}
        return null;
    }
    
    public static ObjectPlayerAccount createPlayerProfile(final String username, final String email, final String password) {
    	try {
    		if(doesProfileExistByEMail(email)) return null;
    		if(doesProfileExistByUsername(username)) return null;
			if(username == null || email == null || password !=null) return null;
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
    		if(profile.username.equals(username)) return true;
    	}
    	return false;
    }
    
    public static boolean doesProfileExistByUUID(final String givenUUID) {
        final File profileFile = new File(NetworkDirectories.profilesDirectory, givenUUID + ".json");
        return profileFile.exists() && profileFile.length() > 0L;
    }
    
    public static boolean isAuthCodeValid(final String givenUUID, final String givenAuthCode) {
        if (doesProfileExistByUUID(givenUUID)) { return getProfileFromUUID(givenUUID).discordAuthCode.equals(givenAuthCode); }
        return false;
    }
}

package com.photon.objects;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.photon.Directories;
import com.photon.PhotonEngine;

import niwer.lumen.Console;
import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

/**
 * Can't use record here, Too complex object for record
 */
public class ObjectPlayerAccount extends SQLSerializable<ObjectPlayerAccount> {

    @IColumnField(name = "username", notNull = true)
	public String username;

    @IColumnField(name = "email", notNull = true)
	public String email;

    @IColumnField(name = "password", notNull = true)
	public String password;

    @IColumnField(name = "twoAuthFactor")
	public boolean twoAuthFactor = false;

    @IColumnField(name = "uuid", notNull = true)
	public String uuid;
	
    @IColumnField(name = "discordID", charLimit = 1024)
    public String discordID;

    @IColumnField(name = "discordAuthCode", notNull = true)
    public String discordAuthCode;
    
    
    @IColumnField(name = "projectAuthor")
    public boolean projectAuthor = false;

    @IColumnField(name = "serverCreator")
    public boolean serverCreator = false;

    @IColumnField(name = "shopCoins")
    public int shopCoins = 0;

    @IColumnField(name = "friends")
    public String firends = "[]"; // JSON Array of UUID friends
    
    public ObjectPlayerAccount() {
    	this.discordAuthCode = generateAuthCode();
    }
    
    public static String generateAuthCode() { return new BigInteger(40, new SecureRandom()).toString(32); }

    public boolean hasDiscordLinked() {
        return this.discordID != null && !this.discordID.isEmpty();
    }

    public List<String> getFriendsList() {
        if (this.firends == null || this.firends.isEmpty()) return new ArrayList<>();
        try {
            TypeToken<ArrayList<String>> typeToken = new TypeToken<ArrayList<String>>() {};
            return Directories.GSON.fromJson(this.firends, typeToken.getType());
        } catch (Exception e) {
            Console.log("Error parsing friends JSON for " + this.uuid + ": " + e.getMessage()).error().container(PhotonEngine.LOGGER).send();
            return new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return "ObjectPlayerAccount [username=" + username + ", email=" + email + ", password=" + password
                + ", twoAuthFactor=" + twoAuthFactor + ", uuid=" + uuid + ", discordID=" + discordID
                + ", discordAuthCode=" + discordAuthCode + ", projectAuthor=" + projectAuthor
                + ", serverCreator=" + serverCreator + ", shopCoins=" + shopCoins + ", firends=" + firends + "]";
    }
}
package com.photon.network.objects;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.gson.reflect.TypeToken;
import com.photon.PhotonEngine;
import com.photon.network.NetworkDirectories;
import com.photon.network.sql.SQLInteraction.SQLCommandSerializer;

import niwer.lumen.Console;

/**
 * Can't use record here, Too complex object for record
 */
public class ObjectPlayerAccount implements SQLCommandSerializer<ObjectPlayerAccount> {
	public String username;
	public String email;
	public String password;
	public boolean twoAuthFactor = false;
	public String uuid;
	
    public String discordID;
    public String discordAuthCode;
    
    public boolean projectAuthor = false;
    public boolean serverCreator = false;
    public int shopCoins = 0;
    public ArrayList<String> firends = new ArrayList<String>();
    
    public ObjectPlayerAccount() {
    	this.discordAuthCode = generateAuthCode();
    }
    
    public static String generateAuthCode() { return new BigInteger(40, new SecureRandom()).toString(32); }

    public boolean hasDiscordLinked() {
        return this.discordID != null && !this.discordID.isEmpty();
    }

    @Override
    public ObjectPlayerAccount objectify(ResultSet resultSet) throws SQLException {
        final ObjectPlayerAccount account = new ObjectPlayerAccount();
        account.username = resultSet.getString("username");
        account.email = resultSet.getString("email");
        account.password = resultSet.getString("password");
        account.twoAuthFactor = resultSet.getBoolean("twoAuthFactor");
        account.uuid = resultSet.getString("uuid");
        account.discordID = resultSet.getString("discordID");
        account.discordAuthCode = resultSet.getString("discordAuthCode");
        account.projectAuthor = resultSet.getBoolean("projectAuthor");
        account.serverCreator = resultSet.getBoolean("serverCreator");
        account.shopCoins = resultSet.getInt("shopCoins");

        final String friendsJSON = resultSet.getString("friends");
        if (friendsJSON != null && !friendsJSON.isEmpty()) {
             try {
                TypeToken<ArrayList<String>> typeToken = new TypeToken<ArrayList<String>>() {};
                account.firends = NetworkDirectories.GSON.fromJson(friendsJSON, typeToken.getType());
            } catch (Exception e) {
                Console.log("Error parsing friends JSON for " + account.uuid + ": " + e.getMessage()).error().container(PhotonEngine.LOGGER).send();
            }
        }

        return account;
    }

    @Override
    public String toString() {
        return "ObjectPlayerAccount [username=" + username + ", email=" + email + ", password=" + password
                + ", twoAuthFactor=" + twoAuthFactor + ", uuid=" + uuid + ", discordID=" + discordID
                + ", discordAuthCode=" + discordAuthCode + ", projectAuthor=" + projectAuthor
                + ", serverCreator=" + serverCreator + ", shopCoins=" + shopCoins + ", firends=" + firends + "]";
    }
}
package com.photon.network.objects;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

public class ObjectPlayerAccount {
	public String username;
	public String email;
	public String password;
	public boolean twoAuthFactor = false;
	public String uuid;
	
    public String discordID;
    public String discordAuthCode;
    
    public boolean projectCreator = false;
    public int shopCoins = 0;
    public ArrayList<String> firends = new ArrayList<String>();
    
    public ObjectPlayerAccount() {
    	this.discordAuthCode = generateAuthCode();
    }
    
    public static String generateAuthCode() { return new BigInteger(40, new SecureRandom()).toString(32); }
}

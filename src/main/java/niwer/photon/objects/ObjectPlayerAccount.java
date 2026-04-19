package niwer.photon.objects;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;

import niwer.lumen.Console;
import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectPlayerAccount extends SQLSerializable<ObjectPlayerAccount> {

    @IColumnField(name = "username", notNull = true)
	private String username;

    @IColumnField(name = "email", notNull = true)
	private String email;

    @IColumnField(name = "password", notNull = true)
	private String password;

    @IColumnField(name = "twoAuthFactor")
	private boolean twoAuthFactor = false;

    @IColumnField(name = "uuid", notNull = true)
	private String uuid;
	
    @IColumnField(name = "discordID", charLimit = 1024)
    private String discordID;

    @IColumnField(name = "discordAuthCode", notNull = true)
    private String discordAuthCode = generateAuthCode();
    
    @IColumnField(name = "projectAuthor")
    private boolean projectAuthor = false;

    @IColumnField(name = "serverCreator")
    private boolean serverCreator = false;

    @IColumnField(name = "shopCoins")
    private int shopCoins = 0;

    @IColumnField(name = "friends")
    private String firends = "[]"; // JSON Array of UUID friends

    public static String generateAuthCode() { return new BigInteger(40, new SecureRandom()).toString(32); }

    public boolean hasDiscordLinked() { return this.discordID != null && !this.discordID.isEmpty(); }

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

    public String username() { return this.username; }

    public String email() { return this.email; }

    public String password() { return this.password; }

    public boolean twoAuthFactor() { return this.twoAuthFactor; }

    public String uuid() { return this.uuid; }

    public String discordID() { return this.discordID; }

    public String discordAuthCode() { return this.discordAuthCode; }

    public boolean projectAuthor() { return this.projectAuthor; }

    public boolean serverCreator() { return this.serverCreator; }

    public int shopCoins() { return this.shopCoins; }
}
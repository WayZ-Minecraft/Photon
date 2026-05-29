package niwer.photon.objects;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectPlayerAccount extends SQLSerializable<ObjectPlayerAccount> {

    @IColumnField(name = "username", notNull = true)
	private String username;

    @IColumnField(name = "email", notNull = true)
	private String email;

    @IColumnField(name = "password", notNull = true)
    private transient String password; // This field should not be serialized when sent to clients for security reasons.

    @IColumnField(name = "twoAuthFactor")
	private boolean twoAuthFactor = false;

    @IColumnField(name = "uuid", notNull = true)
	private String uuid;
	
    @IColumnField(name = "discordID", charLimit = 1024)
    private String discordID;

    @IColumnField(name = "discordAuthCode", notNull = true)
    private String discordAuthCode = generateAuthCode();
    
    @IColumnField(name = "administrator")
    private boolean administrator = false;

    @IColumnField(name = "serverCreator")
    private boolean serverCreator = false;

    public static String generateAuthCode() { return new BigInteger(40, new SecureRandom()).toString(32); }

    public boolean hasDiscordLinked() { return this.discordID != null && !this.discordID.isEmpty(); }

    @Override
    public String toString() {
        return "ObjectPlayerAccount [username=" + username + ", email=" + email + ", password=" + password
                + ", twoAuthFactor=" + twoAuthFactor + ", uuid=" + uuid + ", discordID=" + discordID
                + ", discordAuthCode=" + discordAuthCode + ", administrator=" + administrator
                + ", serverCreator=" + serverCreator + "]";
    }

    public String username() { return this.username; }

    public String getUsername() { return this.username; }

    public String email() { return this.email; }

    public String getEmail() { return this.email; }

    public String password() { return this.password; }

    public boolean twoAuthFactor() { return this.twoAuthFactor; }

    public boolean isTwoAuthFactor() { return this.twoAuthFactor; }

    public String uuid() { return this.uuid; }

    public String getUuid() { return this.uuid; }

    public String discordID() { return this.discordID; }

    public String getDiscordID() { return this.discordID; }

    public String discordAuthCode() { return this.discordAuthCode; }

    public String getDiscordAuthCode() { return this.discordAuthCode; }

    public boolean administrator() { return this.administrator; }

    public boolean isadministrator() { return this.administrator; }

    public boolean serverCreator() { return this.serverCreator; }

    public boolean isServerCreator() { return this.serverCreator; }

    public Map<String, Object> toPublicMap() {
        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", this.username);
        response.put("email", this.email);
        response.put("twoAuthFactor", this.twoAuthFactor);
        response.put("uuid", this.uuid);
        response.put("discordID", this.discordID);
        response.put("discordAuthCode", this.discordAuthCode);
        response.put("administrator", this.administrator);
        response.put("serverCreator", this.serverCreator);
        return response;
    }
}
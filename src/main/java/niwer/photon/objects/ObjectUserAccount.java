package niwer.photon.objects;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectUserAccount extends SQLSerializable<ObjectUserAccount> {

    @IColumnField(name = "username", notNull = true, unique = true)
    private String username;

    @IColumnField(name = "email", notNull = true, unique = true)
    private String email;

    @IColumnField(name = "password", notNull = true)
    private transient String password;

    @IColumnField(name = "uuid", primaryKey = true, notNull = true)
    private String uuid;

    @IColumnField(name = "discordID", charLimit = 1024)
    private String discordID;

    @IColumnField(name = "discordAuthCode", notNull = true, charLimit = 255)
    private String discordAuthCode = generateAuthCode();

    @IColumnField(name = "administrator")
    private boolean administrator;

    public ObjectUserAccount() {}

    public ObjectUserAccount(String username, String email, String uuid, String discordID, boolean administrator) {
        this.username = username;
        this.email = email;
        this.uuid = uuid;
        this.discordID = discordID;
        this.administrator = administrator;
    }

    public static String generateAuthCode() { return new BigInteger(40, new SecureRandom()).toString(32); }

    public boolean hasDiscordLinked() { return this.discordID != null && !this.discordID.isEmpty(); }

    @Override
    public String toString() {
        return String.format("User Account{username='%s', email='%s', uuid='%s', discordID='%s', administrator=%s, serverCreator=%s}", this.username, this.email, this.uuid, this.discordID, isAdministrator());
    }

    public String getUsername() { return this.username; }

    public String getEmail() { return this.email; }

    public String password() { return this.password; }

    public String getUuid() { return this.uuid; }

    public String getDiscordID() { return this.discordID; }

    public String getDiscordAuthCode() { return this.discordAuthCode; }

    public boolean isAdministrator() { return this.administrator; }
    public boolean getAdministrator() { return this.administrator; }

    public Map<String, Object> toPublicMap() {
        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", this.username);
        response.put("email", this.email);
        response.put("uuid", this.uuid);
        response.put("discordID", this.discordID);
        response.put("discordAuthCode", this.discordAuthCode);
        response.put("administrator", isAdministrator());
        return response;
    }
}
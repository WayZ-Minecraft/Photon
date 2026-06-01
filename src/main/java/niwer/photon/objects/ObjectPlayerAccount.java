package niwer.photon.objects;

import java.lang.reflect.Field;
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
    private transient String password;

    @IColumnField(name = "uuid", notNull = true)
    private String uuid;

    @IColumnField(name = "discordID", charLimit = 1024)
    private String discordID;

    @IColumnField(name = "discordAuthCode", notNull = true)
    private String discordAuthCode = generateAuthCode();

    @IColumnField(name = "administrator")
    private boolean administrator;

    @IColumnField(name = "serverCreator")
    private boolean serverCreator;

    public static String generateAuthCode() { return new BigInteger(40, new SecureRandom()).toString(32); }

    public boolean hasDiscordLinked() { return this.discordID != null && !this.discordID.isEmpty(); }

    @Override
    public String toString() {
        return String.format("ObjectPlayerAccount{username='%s', email='%s', uuid='%s', discordID='%s', administrator=%s, serverCreator=%s}", 
            this.username, this.email, this.uuid, this.discordID, isAdministrator(), isServerCreator());
    }

    public String getUsername() { return this.username; }

    public String getEmail() { return this.email; }

    public String password() { return this.password; }

    public String getUuid() { return this.uuid; }

    public String getDiscordID() { return this.discordID; }

    public String getDiscordAuthCode() { return this.discordAuthCode; }

    public boolean isAdministrator() { return this.administrator; }
    public boolean getAdministrator() { return this.administrator; }

    public boolean isServerCreator() { return this.serverCreator; }
    public boolean getServerCreator() { return this.serverCreator; }

    public Map<String, Object> toPublicMap() {
        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", this.username);
        response.put("email", this.email);
        response.put("uuid", this.uuid);
        response.put("discordID", this.discordID);
        response.put("discordAuthCode", this.discordAuthCode);
        response.put("administrator", isAdministrator());
        response.put("serverCreator", isServerCreator());
        return response;
    }

    public static ObjectPlayerAccount fromSnapshot(String username, String email, String uuid, String discordID, String discordAuthCode, boolean administrator, boolean serverCreator) {
        final ObjectPlayerAccount account = new ObjectPlayerAccount();
        try {
            setField(account, "username", username);
            setField(account, "email", email);
            setField(account, "uuid", uuid);
            setField(account, "discordID", discordID);
            setField(account, "discordAuthCode", discordAuthCode);
            setField(account, "administrator", administrator);
            setField(account, "serverCreator", serverCreator);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to rebuild account snapshot", e);
        }
        return account;
    }

    private static void setField(ObjectPlayerAccount account, String fieldName, Object value) throws ReflectiveOperationException {
        final Field field = ObjectPlayerAccount.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        if (field.getType() == boolean.class && value instanceof Boolean booleanValue) {
            field.setBoolean(account, booleanValue);
            return;
        }
        field.set(account, value);
    }

    private static boolean isTruthy(String value) {
        return value != null && (value.equalsIgnoreCase("true") || value.equals("1") || value.equalsIgnoreCase("yes"));
    }
}
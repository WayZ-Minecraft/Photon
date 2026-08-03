package niwer.photon.util.session;

public class AccountSnapshot {
    String username;
    String email;
    String uuid;
    String discordID;
    String discordAuthCode;
    boolean administrator;
    boolean serverCreator;

    public AccountSnapshot(String username, String email, String uuid, String discordID, String discordAuthCode, boolean administrator, boolean serverCreator) {
        this.username = username;
        this.email = email;
        this.uuid = uuid;
        this.discordID = discordID;
        this.discordAuthCode = discordAuthCode;
        this.administrator = administrator;
        this.serverCreator = serverCreator;
    }

    public String username() { return this.username; }

    public String email() { return this.email; }

    public String uuid() { return this.uuid; }

    public String discordID() { return this.discordID; }

    public String discordAuthCode() { return this.discordAuthCode; }

    public boolean administrator() { return this.administrator; }

    public boolean serverCreator() { return this.serverCreator; }
}
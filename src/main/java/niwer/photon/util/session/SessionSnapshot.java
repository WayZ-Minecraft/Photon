package niwer.photon.util.session;

import niwer.photon.objects.ObjectUserAccount;

final class SessionSnapshot {
    long createdAt;
    String csrf = null;

    /* Account snapshot data */
    String username;
    String email;
    String uuid;
    String discordID;
    boolean administrator;

    public SessionSnapshot(ObjectUserAccount account, long createdAt, String csrf) {
        this.createdAt = createdAt;
        this.csrf = csrf;

        this.username = account.getUsername();
        this.email = account.getEmail();
        this.uuid = account.getUuid();
        this.discordID = account.getDiscordID();
        this.administrator = account.isAdministrator();
    }

    public long createdAt() { return this.createdAt; }

    public String csrf() { return this.csrf; }

    public String username() { return this.username; }

    public String email() { return this.email; }

    public String uuid() { return this.uuid; }

    public String discordID() { return this.discordID; }

    public boolean administrator() { return this.administrator; }

    public ObjectUserAccount account() {
        return new ObjectUserAccount(
            this.username,
            this.email,
            this.uuid,
            this.discordID,
            this.administrator
        );
    }
}
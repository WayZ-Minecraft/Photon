package niwer.photon.util.session;

import niwer.photon.objects.ObjectUserAccount;

public class SessionSnapshot {
    ObjectUserAccount account;
    long createdAt;
    String csrf = null;

    public SessionSnapshot(ObjectUserAccount account, long createdAt) {
        this(account, createdAt, null);
    }

    public SessionSnapshot(ObjectUserAccount account, long createdAt, String csrf) {
        this.account = account;
        this.createdAt = createdAt;
        this.csrf = csrf;
    }

    public ObjectUserAccount account() { return this.account; }

    public long createdAt() { return this.createdAt; }

    public String csrf() { return this.csrf; }
}
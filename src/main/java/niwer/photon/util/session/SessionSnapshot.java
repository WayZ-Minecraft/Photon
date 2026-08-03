package niwer.photon.util.session;

public class SessionSnapshot {
    AccountSnapshot account;
    long createdAt;
    String csrf = null;

    public SessionSnapshot(AccountSnapshot account, long createdAt) {
        this(account, createdAt, null);
    }

    public SessionSnapshot(AccountSnapshot account, long createdAt, String csrf) {
        this.account = account;
        this.createdAt = createdAt;
        this.csrf = csrf;
    }

    public AccountSnapshot account() { return this.account; }

    public long createdAt() { return this.createdAt; }

    public String csrf() { return this.csrf; }
}
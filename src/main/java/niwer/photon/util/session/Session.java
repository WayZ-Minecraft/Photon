package niwer.photon.util.session;

import niwer.photon.objects.ObjectUserAccount;

public class Session {
    String token;
    ObjectUserAccount account;

    public Session(String token, ObjectUserAccount account) {
        this.token = token;
        this.account = account;
    }

    public String token() { return this.token; }

    public ObjectUserAccount account() { return this.account; }
}
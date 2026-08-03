package niwer.photon.util.session;

import niwer.photon.objects.ObjectPlayerAccount;

public class AuthSession {
    String token;
    ObjectPlayerAccount account;

    public AuthSession(String token, ObjectPlayerAccount account) {
        this.token = token;
        this.account = account;
    }

    public String token() { return this.token; }

    public ObjectPlayerAccount account() { return this.account; }
}
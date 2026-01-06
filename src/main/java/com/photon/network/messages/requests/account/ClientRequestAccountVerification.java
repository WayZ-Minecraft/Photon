package com.photon.network.messages.requests.account;

/**
 * @author noz43
 */
public class ClientRequestAccountVerification {
    private final String email;
    private final String password;
    
    public ClientRequestAccountVerification(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
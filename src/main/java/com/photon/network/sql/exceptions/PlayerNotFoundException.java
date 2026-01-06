package com.photon.network.sql.exceptions;

public class PlayerNotFoundException extends Exception {
    public PlayerNotFoundException(String errorMessage) {
        super(errorMessage);
    }
    
}
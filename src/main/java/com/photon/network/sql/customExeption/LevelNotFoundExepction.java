package com.photon.network.sql.customExeption;

public class LevelNotFoundExepction extends Exception {
    public LevelNotFoundExepction(String errorMessage) {
        super(errorMessage);
    }
}
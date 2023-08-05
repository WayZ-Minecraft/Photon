package com.photon.discord;

/**
 * enume to definde channels
 */
public enum Channels {
    //Special bot channels
    TEXT_BOT(1134112188768997528L),
    CONSOLE_NETWROK(1134225343985762364L);

    public long id;

    private Channels(long id) {
        this.id = id;
    }
}

package com.photon.discord;

/**
 * enume to definde channels
 */
public enum Channels {
    // Special bot channels
    TEXT_BOT(1323747366792069131L),
    CONSOLE_NETWROK(1323747366792069131L);

    public long id;

    private Channels(long id) {
        this.id = id;
    }
}

package com.photon.discord.richpresence.callback;

import com.sun.jna.Callback;

public interface DisconnectedCallback extends Callback { void apply(int errorCode, String message); }

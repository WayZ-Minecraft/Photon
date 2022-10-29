package com.photon.discord.richpresence.callback;

import com.sun.jna.Callback;

public interface ErroredCallback extends Callback { void apply(int errorCode, String message); }

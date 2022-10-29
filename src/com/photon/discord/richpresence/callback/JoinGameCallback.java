package com.photon.discord.richpresence.callback;

import com.sun.jna.Callback;

public interface JoinGameCallback extends Callback {

	void apply(String joinSecret);
}

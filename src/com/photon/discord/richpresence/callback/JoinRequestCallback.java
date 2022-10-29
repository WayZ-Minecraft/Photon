package com.photon.discord.richpresence.callback;

import com.photon.discord.richpresence.DiscordUser;
import com.sun.jna.Callback;

public interface JoinRequestCallback extends Callback {

	void apply(DiscordUser request);
}

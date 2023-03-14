package com.photon.discord.richpresence.callback;

import com.photon.discord.richpresence.DiscordUser;
import com.sun.jna.Callback;

public interface ReadyCallback extends Callback {

	void apply(DiscordUser user);
}


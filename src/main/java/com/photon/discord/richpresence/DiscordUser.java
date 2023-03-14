package com.photon.discord.richpresence;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class DiscordUser extends Structure {

	public String userId;
	public String username;
	public String discriminator;
	public String avatar;

	@Override
	public List<String> getFieldOrder() {
		return Arrays.asList("userId", "username", "discriminator", "avatar");
	}
}
package com.photon.discord.richpresence;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class DiscordRichPresence extends Structure {

	public String state;
	public String details;
	public long startTimestamp;
	public long endTimestamp;
	public String largeImageKey;
	public String largeImageText;
	public String smallImageKey;
	public String smallImageText;
	public String partyId;
	public int partySize;
	public int partyMax;
	@Deprecated
	public String matchSecret;
	public String spectateSecret;
	public String joinSecret;
	@Deprecated
	public int instance;

	@Override
	public List<String> getFieldOrder() {
		return Arrays.asList("state", "details", "startTimestamp", "endTimestamp", "largeImageKey", "largeImageText", "smallImageKey", "smallImageText", "partyId", "partySize", "partyMax", "matchSecret", "joinSecret", "spectateSecret", "instance");
	}

	public static class Builder {

		private DiscordRichPresence p;

		public Builder(String state) {
			p = new DiscordRichPresence();
			p.state = state;
		}

		public Builder setDetails(String details) {
			p.details = details;
			return this;
		}

		public Builder setStartTimestamps(long start) {
			p.startTimestamp = start;
			return this;
		}

		public Builder setEndTimestamp(long end) {
			p.endTimestamp = end;
			return this;
		}

		public Builder setBigImage(String key, String text) {
			if ((text != null && !text.equalsIgnoreCase("")) && key == null)
				throw new IllegalArgumentException("Image key must not be null when assigning a hover text.");

			p.largeImageKey = key;
			p.largeImageText = text;
			return this;
		}

		public Builder setSmallImage(String key, String text) {
			if ((text != null && !text.equalsIgnoreCase("")) && key == null)
				throw new IllegalArgumentException("Image key must not be null when assigning a hover text.");

			p.smallImageKey = key;
			p.smallImageText = text;
			return this;
		}

		public Builder setParty(String party, int size, int max) {
			p.partyId = party;
			p.partySize = size;
			p.partyMax = max;
			return this;
		}

		@Deprecated
		public Builder setSecrets(String match, String join, String spectate) {
			p.matchSecret = match;
			p.joinSecret = join;
			p.spectateSecret = spectate;
			return this;
		}

		public Builder setSecrets(String join, String spectate) {
			p.joinSecret = join;
			p.spectateSecret = spectate;
			return this;
		}

		@Deprecated
		public Builder setInstance(boolean i) {
			p.instance = i ? 1 : 0;
			return this;
		}

		public DiscordRichPresence build() { return p; }
	}
}

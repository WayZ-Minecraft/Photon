package com.photon.discord.richpresence;

import java.util.Arrays;
import java.util.List;

import com.photon.discord.richpresence.callback.DisconnectedCallback;
import com.photon.discord.richpresence.callback.ErroredCallback;
import com.photon.discord.richpresence.callback.JoinGameCallback;
import com.photon.discord.richpresence.callback.JoinRequestCallback;
import com.photon.discord.richpresence.callback.ReadyCallback;
import com.photon.discord.richpresence.callback.SpectateGameCallback;
import com.sun.jna.Structure;

public class DiscordEventHandlers extends Structure {

	public ReadyCallback ready;
	public DisconnectedCallback disconnected;
	public ErroredCallback errored;
	public JoinGameCallback joinGame;
	public SpectateGameCallback spectateGame;
	public JoinRequestCallback joinRequest;

	@Override
	public List<String> getFieldOrder() { return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest"); }

	public static class Builder {

		DiscordEventHandlers h;

		public Builder() {
			h = new DiscordEventHandlers();
		}

		public Builder setReadyEventHandler(ReadyCallback r) {
			h.ready = r;
			return this;
		}

		public Builder setDisconnectedEventHandler(DisconnectedCallback d) {
			h.disconnected = d;
			return this;
		}

		public Builder setErroredEventHandler(ErroredCallback e) {
			h.errored = e;
			return this;
		}

		public Builder setJoinGameEventHandler(JoinGameCallback j) {
			h.joinGame = j;
			return this;
		}

		public Builder setSpectateGameEventHandler(SpectateGameCallback s) {
			h.spectateGame = s;
			return this;
		}

		public Builder setJoinRequestEventHandler(JoinRequestCallback j) {
			h.joinRequest = j;
			return this;
		}

		public DiscordEventHandlers build() {
			return h;
		}
	}
}

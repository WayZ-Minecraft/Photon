package com.photon.discord.richpresence;

import java.util.Timer;
import java.util.TimerTask;

import com.photon.informations.PhotonInfosManager;

public abstract class DiscordRPEngine extends TimerTask {

    private DiscordRichPresence presence = new DiscordRichPresence();
    private DiscordEventHandlers handlers = new DiscordEventHandlers();
    private Timer ticker = new Timer();
    
    public void init(boolean systemTimestamp) { this.init(systemTimestamp, false, 5000L); }
    
	public void init(boolean systemTimestamp, boolean update, long period) {
		if(PhotonInfosManager.getInfos() == null) return;
		DiscordRPC.discordInitialize(PhotonInfosManager.getInfos().discord_bot_id, handlers, true);
		if(systemTimestamp) { presence.startTimestamp = System.currentTimeMillis() / 1000L; }
		updatePresence();
		new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				DiscordRPC.discordRunCallbacks();
				try { Thread.sleep(2000L); } catch (InterruptedException ex) {}
			}
		}, "RPC-Callback-Handler").start();
		if(update) { ticker.schedule(this, 0L, period); }
	}
	
	public DiscordRichPresence getPresence() { return presence; }
	
	public void updatePresence() { DiscordRPC.discordUpdatePresence(presence); }

	@Override
	public void run() { onTick(); }
	
	public abstract void onTick();
}

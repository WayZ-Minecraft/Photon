package com.photon.network.messages.requests;

import java.io.File;

import com.esotericsoftware.kryonet.Connection;
import com.photon.discord.BotEngine;
import com.photon.network.IPacket;
import com.photon.util.ConsoleManager.EnumLogType;

import niwer.lumen.Console;

/**
 * @author noz43 & Niwer
 */
public class ClientRequestSendDiscordLogs implements IPacket {
    private final Console DATA;
    
    public ClientRequestSendDiscordLogs() { this.DATA = null; }

    public ClientRequestSendDiscordLogs(Console data) { this.DATA = data; }
    
    public Console data() { return this.DATA; }

    @Override
    public void handle(Connection connection) {
        if (BotEngine.isBotInitialized()) BotEngine.log(this.DATA);
    }
}
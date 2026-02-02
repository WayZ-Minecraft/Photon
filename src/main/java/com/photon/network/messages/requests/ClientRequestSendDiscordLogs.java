package com.photon.network.messages.requests;

import java.io.File;

import com.esotericsoftware.kryonet.Connection;
import com.photon.discord.BotEngine;
import com.photon.network.IPacket;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author noz43
 */

public class ClientRequestSendDiscordLogs implements IPacket {
    private final EnumLogType type;
    private final String subType;
    private final Object content;
    private final File file;
    
    public ClientRequestSendDiscordLogs() {
        this.type = null;
        this.subType = null;
        this.content = null;
        this.file = null;
    }

    public ClientRequestSendDiscordLogs(EnumLogType type, String subType, Object content, File file) {
        this.type = type;
        this.subType = subType;
        this.content = content;
        this.file = file;
    }
    
    public EnumLogType getType() { return type; }
    public String getSubType() { return subType; }
    public Object getContent() { return content; }
    public File getFile() { return file; }
    
    @Override
    public void handle(Connection connection) {
        if (BotEngine.isBotInitialized()) BotEngine.log(type.color, type + " : " + subType, content, file);
    }
}
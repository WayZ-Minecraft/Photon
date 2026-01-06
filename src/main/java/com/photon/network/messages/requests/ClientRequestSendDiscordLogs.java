package com.photon.network.messages.requests;

import java.io.File;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author noz43
 */
public class ClientRequestSendDiscordLogs {
    private final EnumLogType type;
    private final String subType;
    private final Object content;
    private final File file;
    
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
}
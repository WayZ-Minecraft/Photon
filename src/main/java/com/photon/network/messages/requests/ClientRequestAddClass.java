package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.network.NetworkObjectRegistry;
import com.photon.util.ConsoleManager;
import com.photon.util.ConsoleManager.EnumLogType;

/**
 * @author noz43
 */

public class ClientRequestAddClass {
    private final String name;
    private final byte[] bytes;
    
    public ClientRequestAddClass(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
    }
    
    public String getName() { return name; }
    public byte[] getBytes() { return bytes; }
    
    public void handle(Connection connection) {
        NetworkObjectRegistry.addClass(name, bytes);
        
        ConsoleManager.create("Network class registered: " + name)
            .withType(EnumLogType.NETWORK)
            .end();
    }
}
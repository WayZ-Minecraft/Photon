package com.photon.network.messages.requests;

import com.esotericsoftware.kryonet.Connection;
import com.photon.PhotonEngine;
import com.photon.network.IPacket;
import com.photon.network.NetworkObjectRegistry;
import com.photon.util.PhotonLogTypes;

import niwer.lumen.Console;

/**
 * @author Niwer
 * @author noz43
 */
public class ClientRequestAddClass implements IPacket {
    private final String name;
    private final byte[] bytes;
    
    public ClientRequestAddClass() {
        this.name = null;
        this.bytes = null;
    }

    public ClientRequestAddClass(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
    }
    
    public String getName() { return name; }
    public byte[] getBytes() { return bytes; }
    
    public void handle(Connection connection) {
        NetworkObjectRegistry.addClass(name, bytes);
        
        Console.log("Network class registered: " + name)
            .type(PhotonLogTypes.NETWORK)
            .container(PhotonEngine.LOGGER)
            .send();
    }
}
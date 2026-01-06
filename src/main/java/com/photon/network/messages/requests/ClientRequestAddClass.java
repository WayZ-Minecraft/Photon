package com.photon.network.messages.requests;

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
}
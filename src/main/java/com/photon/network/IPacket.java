package com.photon.network;

import com.esotericsoftware.kryonet.Connection;

/**
 * Used to handle packet data on both client and server sides.
 * @author Niwer
 */
public interface IPacket {
    public void handle(Connection connection);
}

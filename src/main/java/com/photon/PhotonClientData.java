package com.photon;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.photon.network.messages.response.account.ServerResponseValidAccount;
import com.photon.objects.ObjectNews;
import com.photon.objects.ObjectPlayerAccount;
import com.photon.objects.ObjectServer;
import com.photon.util.AsyncValue;

/**
 * This class allows to get all kind of data from the official network.
 * E.G : News, Servers, Player Account, etc.
 */
public class PhotonClientData {

    public static final Set<ObjectServer> CLIENT_SERVER_LIST = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static final Set<ObjectNews> CLIENT_NEWS_LIST = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static final AsyncValue<ObjectPlayerAccount> PLAYER_ACCOUNT = new AsyncValue<>();
    public static final AsyncValue<ServerResponseValidAccount> PLAYER_ACCOUNT_VERIF = new AsyncValue<>();
    public static final AsyncValue<byte[]> UPDATE_DATA = new AsyncValue<>(new byte[0]);
    public static final AsyncValue<String> UPDATE_SHA = new AsyncValue<>("UNKNOWN");
}

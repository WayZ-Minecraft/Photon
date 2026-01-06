package com.photon.network.messages.response;

import java.util.ArrayList;
import java.util.List;

import com.photon.network.objects.ObjectNews;

/**
 * @author noz43
 */
public class ServerResponseNewsList {
    private final List<ObjectNews> newsObjects;
    
    public ServerResponseNewsList(List<ObjectNews> newsObjects) {
        this.newsObjects = new ArrayList<>(newsObjects);
    }
    
    public ArrayList<ObjectNews> getNewsObjects() { return new ArrayList<>(newsObjects); }
}

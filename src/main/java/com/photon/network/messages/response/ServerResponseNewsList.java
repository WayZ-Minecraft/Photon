package com.photon.network.messages.response;

import java.util.ArrayList;
import com.photon.network.objects.ObjectNews;

/**
 * @author noz43
 */
public class ServerResponseNewsList {
    private final ArrayList<ObjectNews> newsObjects;
    
    public ServerResponseNewsList(ArrayList<ObjectNews> newsObjects) {
        this.newsObjects = new ArrayList<>(newsObjects);
    }
    
    public ArrayList<ObjectNews> getNewsObjects() { return new ArrayList<>(newsObjects); }
}

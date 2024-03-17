package com.photon.network.objects;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

public class ObjectHWIDs
{
    private static final Gson GSON;
    public Set<HWID> hwids;
    
    public ObjectHWIDs() {
        this.hwids = new HashSet<HWID>();
    }
    
    public static void create(final File file) {
        new ObjectHWIDs().save(file);
    }
    
    public void save(final File file) {
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(ObjectHWIDs.GSON.toJson(this));
            writer.close();
        }
        catch (final JsonIOException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public static ObjectHWIDs load(final File file) {
        try {
            return ObjectHWIDs.GSON.<ObjectHWIDs>fromJson(new BufferedReader(new FileReader(file)), ObjectHWIDs.class);
        }
        catch (final IOException e) {
            e.printStackTrace();
            return new ObjectHWIDs();
        }
    }
    
    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public static class HWID
    {
        public String userName;
        public String userUUID;
        public String userHWID;
        public String operatingSystem;
        
        public HWID(final String userName, final String userUUID, final String userHWID, final String operatingSystem) {
            this.userName = userName;
            this.userUUID = userUUID;
            this.userHWID = userHWID;
            this.operatingSystem = operatingSystem;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof HWID hwid)
                return hwid.userName.equals(this.userName) && hwid.userUUID.equals(this.userUUID) && hwid.userHWID.equals(this.userHWID) && hwid.operatingSystem.equals(this.operatingSystem);
            return super.equals(obj);
        }
    }
}

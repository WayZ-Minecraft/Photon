package com.photon.network.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.photon.discord.usersInteraction.Languages;
import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager;

/**
 * Object that contains all the discord information about the users 
 */
public class ObjectDiscord {
    public HashMap<String, UserInfo> Users = new HashMap<>();

    static Gson  gson = new GsonBuilder().create();

    /**
     * Save the object in a json file (firstConnection.json)
     * @param object the object to save
     */
    public static void save (ObjectDiscord object) {
        try {
            File file = new File(NetworkDirectories.discordDirectory, "firstConnection.json");
            if (!file.exists()) file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(object));
            writer.close();
        } catch (IOException e) {
            ConsoleManager.create("Error while save firstConnection.json").displayOnDiscord().error().end();
        }
    }

    /**
     * Load the object from a json file (firstConnection.json)
     * @return the object loaded
     */
    public static ObjectDiscord load() {
        try {
            File file = new File(NetworkDirectories.discordDirectory, "firstConnection.json");
            if (!file.exists()) save(new ObjectDiscord());
            BufferedReader br = new BufferedReader(new FileReader(file));
            ObjectDiscord object = gson.fromJson(br, ObjectDiscord.class);
            br.close();
            return object;

        } catch (IOException e) {
            ConsoleManager.create("Error while loading firstConnection.json").displayOnDiscord().error().end();
            return null;
        }
    }


    /**
     * Class that contains the information about a user
     */
    public class UserInfo {
        public boolean firstConnection = true;
        public ArrayList<Languages> language = new ArrayList<>();
    }


}

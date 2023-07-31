package com.photon.network.objects.discord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager;

/**
 * Object that mangaes the data of the discord server
 */
public class ObjectDiscord {

    static Gson  gson = new GsonBuilder().create();

    /**
     * Save the object in a json file
     * @param object the object to save
     * @param infoType the type of information (e.g. InfoType.GLOBAL)
     */
    public static void save (ObjectDiscord object, InfoType infoType) {
        try {
            File file = new File(NetworkDirectories.discordDirectory, infoType.getPath()+".json");
            if (!file.exists()) file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(object));
            writer.close();
        } catch (IOException e) {
            ConsoleManager.create(String.format("Error while save %s.json",infoType.getPath())).displayOnDiscord().error().end();
        }
    }

    /**
     * Load the object from a json file
     * @param infoType the type of information (e.g. InfoType.GLOBAL)
     * @return the object loaded
     */
    public static Object load(InfoType infoType) {
        try {
            File file = new File(NetworkDirectories.discordDirectory, infoType.getPath()+".json");
            if (!file.exists()) save(new ObjectDiscord(), infoType);
            BufferedReader br = new BufferedReader(new FileReader(file));
            Object object = gson.fromJson(br, infoType.getObjectClass().getClass());
            br.close();
            return object;

        } catch (IOException e) {
            ConsoleManager.create(String.format("Error while loading %s.json",infoType.getPath())).displayOnDiscord().error().end();
            return null;
        }
    }


}

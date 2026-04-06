package dev.trg.hugoclient.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PearlBotConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final File FILE =
            new File("config/pearlbot.json");

    private static Map<String, PearlData> DATA = new HashMap<>();

    private static final Type DATA_TYPE =
            new TypeToken<Map<String, PearlData>>() {}.getType();

    public static void load() {
        try {
            if (!FILE.exists()) {
                DATA = new HashMap<>();
                return;
            }

            Map<String, PearlData> loaded =
                    GSON.fromJson(new FileReader(FILE), DATA_TYPE);

            DATA = (loaded != null) ? loaded : new HashMap<>();

        } catch (Exception e) {
            e.printStackTrace();
            DATA = new HashMap<>();
        }
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            GSON.toJson(DATA, new FileWriter(FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void put(String name, PearlData data) {
        DATA.put(name, data);
    }

    public static PearlData get(String name) {
        return DATA.get(name);
    }

    public static Map<String, PearlData> getAll() {
        return DATA;
    }
}

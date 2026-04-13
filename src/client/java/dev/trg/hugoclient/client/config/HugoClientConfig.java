package dev.trg.hugoclient.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.trg.hugoclient.client.feature.ClientFeature;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class HugoClientConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final File FILE =
            new File("config/hugoclient.json");

    private static ConfigData data = createDefaultData();

    private HugoClientConfig() {
    }

    public static void load() {
        try {
            if (!FILE.exists()) {
                data = createDefaultData();
                save();
                return;
            }

            try (Reader reader = Files.newBufferedReader(FILE.toPath(), StandardCharsets.UTF_8)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                data = (loaded != null) ? loaded : createDefaultData();
            }

            mergeDefaults();
            save();
        } catch (Exception e) {
            e.printStackTrace();
            data = createDefaultData();
        }
    }

    public static void save() {
        try {
            File parent = FILE.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            try (Writer writer = Files.newBufferedWriter(FILE.toPath(), StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isEnabled(ClientFeature feature) {
        if (feature == null) {
            return false;
        }

        Boolean value = data.features.get(feature.id());
        return (value != null) ? value : feature.defaultEnabled();
    }

    public static void setEnabled(ClientFeature feature, boolean enabled) {
        if (feature == null) {
            return;
        }

        data.features.put(feature.id(), enabled);
        save();
    }

    public static boolean toggle(ClientFeature feature) {
        boolean enabled = !isEnabled(feature);
        setEnabled(feature, enabled);
        return enabled;
    }

    private static ConfigData createDefaultData() {
        ConfigData defaults = new ConfigData();
        for (ClientFeature feature : ClientFeature.values()) {
            defaults.features.put(feature.id(), feature.defaultEnabled());
        }
        return defaults;
    }

    private static void mergeDefaults() {
        if (data.features == null) {
            data.features = new HashMap<>();
        }

        for (ClientFeature feature : ClientFeature.values()) {
            data.features.putIfAbsent(feature.id(), feature.defaultEnabled());
        }
    }

    private static final class ConfigData {
        private Map<String, Boolean> features = new HashMap<>();
    }
}

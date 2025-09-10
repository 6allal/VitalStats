package com.atl.vitalstats.config;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Config {
    private static final Gson GSON = new Gson();
    public static final String CONFIG_FILE = "healthindicators.json";
    private static Config INSTANCE = new Config();

    private boolean heartsRenderingEnabled = true;
    private boolean overrideAllFiltersEnabled = false;

    public static boolean getRenderingEnabled(){
        return INSTANCE.heartsRenderingEnabled;
    }

    public static boolean getHeartsRenderingEnabled() {
        return INSTANCE.heartsRenderingEnabled;
    }

    public static void setHeartsRenderingEnabled(boolean renderingEnabled) {
        INSTANCE.heartsRenderingEnabled = renderingEnabled;
        save();
    }

    public static boolean getOverrideAllFiltersEnabled() {
        return INSTANCE.overrideAllFiltersEnabled;
    }

    public static void setOverrideAllFiltersEnabled(boolean overrideAllFiltersEnabled) {
        INSTANCE.overrideAllFiltersEnabled = overrideAllFiltersEnabled;
        save();
    }


    public static void load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile()))) {
            Config config = GSON.fromJson(reader, Config.class);
            if (config != null) {
                INSTANCE = config;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile()))) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package me.athulsib.stomcheat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigLoader {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ACConfig loadConfig() {
        File file = new File("config.json");
        if (!file.exists()) {
            return createConfig();
        }

        try(FileReader fileReader = new FileReader(file);) {
            return gson.fromJson(fileReader, ACConfig.class);
        } catch (IOException exception) {
            throw new RuntimeException("Could read config.json", exception);
        }
    }

    private ACConfig createConfig() {
        ACConfig config = new ACConfig(true);

        try (FileWriter fileWriter = new FileWriter("config.json")) {
            gson.toJson(config, fileWriter);
        } catch (IOException exception) {
            throw new RuntimeException("Could not write config.json", exception);
        }
        return config;
    }
}

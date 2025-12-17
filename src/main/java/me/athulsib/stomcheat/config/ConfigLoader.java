package me.athulsib.stomcheat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigLoader {
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public Config loadConfig() {
        File file = new File("stomcheat_config.json");
        if (!file.exists()) {
            return createConfig();
        }

        try(FileReader fileReader = new FileReader(file);) {
            return gson.fromJson(fileReader, Config.class);
        } catch (IOException exception) {
            throw new RuntimeException("Could read stomcheat_config.json", exception);
        }
    }

    private Config createConfig() {
        Config config = new Config(
                true,
                true,
                Math.min(Runtime.getRuntime().availableProcessors(), 16),
                "&6&lStomCheat &7&o>> &6%player% &fhas failed &6%check% %type% &8[VL:&r%vl%&7/%punishvl%&8] %experimental%",
                "&c(DEV)",
                """
                &6Details:
                &eCheck: &a%check%
                &eType: &a%type%
                &eViolations: &a%vl%
                &ePing: &a%ping%ms
                &eDescription: &a%description%
                &eData: &a%data%
                """,
                """
                
                &6&lStomCheat &8>> &e%s &bhas been removed from the Network
                &eReason: &cUnfair Advantage
                
                """,
                """
                
                &cYou have been removed from the Network
                &c[StomCheat] Unfair Advantage
                
                """
        );

        try (FileWriter fileWriter = new FileWriter("stomcheat_config.json")) {
            gson.toJson(config, fileWriter);
        } catch (IOException exception) {
            throw new RuntimeException("Could not write stomcheat_config.json", exception);
        }
        return config;
    }
}

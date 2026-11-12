package dev.chain.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.chain.module.Module;
import dev.chain.module.ModuleManager;
import dev.chain.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Сохраняет клавиши и настройки модулей в config/chainclient.json. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigManager() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("chainclient.json");
    }

    public static void save(ModuleManager manager) {
        try {
            JsonObject root = new JsonObject();
            for (Module m : manager.all()) {
                JsonObject o = new JsonObject();
                o.addProperty("key", m.keyCode);
                JsonObject settings = new JsonObject();
                for (Setting s : m.settings) {
                    s.save(settings);
                }
                o.add("settings", settings);
                root.add(m.name, o);
            }
            Files.writeString(file(), GSON.toJson(root));
        } catch (Exception e) {
            System.err.println("[ChainClient] Could not save config: " + e);
        }
    }

    public static void load(ModuleManager manager) {
        try {
            Path path = file();
            if (!Files.exists(path)) {
                return;
            }
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            for (Module m : manager.all()) {
                if (!root.has(m.name)) {
                    continue;
                }
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("key")) {
                    m.keyCode = o.get("key").getAsInt();
                }
                if (o.has("settings")) {
                    JsonObject settings = o.getAsJsonObject("settings");
                    for (Setting s : m.settings) {
                        s.load(settings);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ChainClient] Could not load config: " + e);
        }
    }
}

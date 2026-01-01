package io.github.squid233.remembermymods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.metadata.AbstractModMetadata;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RememberMyMods implements ClientModInitializer {
    private static final Logger log = LoggerFactory.getLogger(RememberMyMods.class);
    private static final Path CONFIG_FILE = Path.of("remember-my-mods.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onInitializeClient() {
        ServerLifecycleEvents.BEFORE_SAVE.register(RememberMyMods::onBeforeSave);
    }

    private static Path getConfigFile(Path worldPath) {
        Path configDir = worldPath.resolve("config");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            log.error("Error creating config directories", e);
            return null;
        }

        return configDir.resolve(CONFIG_FILE);
    }

    private static Path getConfigFile(MinecraftServer server) {
        return getConfigFile(server.getWorldPath(LevelResource.ROOT));
    }

    private static Map<String, String> getAllModVersions() {
        Map<String, String> map = new HashMap<>();
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            ModMetadata metadata = modContainer.getMetadata();
            String type = metadata.getType();
            if (AbstractModMetadata.TYPE_BUILTIN.equals(type)) {
                // do not store Minecraft or Java
                continue;
            }
            // removing client mods does not break your world, so we're only storing server mods
            if (metadata.getEnvironment().matches(EnvType.SERVER)) {
                map.put(metadata.getId(), metadata.getVersion().getFriendlyString());
            }
        }
        return map;
    }

    public static List<ModCompatibilityCheckResult> checkModCompatibility(Path worldPath) {
        Path configFile = getConfigFile(worldPath);
        if (configFile == null) {
            return List.of();
        }

        if (Files.notExists(configFile)) {
            return List.of();
        }

        Map<String, String> oldMods;
        try (BufferedReader reader = Files.newBufferedReader(configFile)) {
            oldMods = GSON.fromJson(reader, new TypeToken<>() {
            });
        } catch (Exception e) {
            log.error("Error reading config file", e);
            return List.of();
        }

        Map<String, String> modVersions = getAllModVersions();
        if (oldMods.equals(modVersions)) {
            return List.of();
        }

        List<ModCompatibilityCheckResult> list = new ArrayList<>();
        // check for mods that are removed
        for (var entry : oldMods.entrySet()) {
            String modId = entry.getKey();
            String versionStr = entry.getValue();
            if (!modVersions.containsKey(modId)) {
                list.add(new ModCompatibilityCheckResult(modId, versionStr, null));
            }
        }
        return list;
    }

    private static void onBeforeSave(MinecraftServer server, boolean flush, boolean force) {
        Path configFile = getConfigFile(server);
        if (configFile == null) {
            return;
        }

        var map = getAllModVersions();

        try {
            Files.writeString(configFile, GSON.toJson(map));
        } catch (Exception e) {
            log.error("Error writing config file", e);
        }
    }
}

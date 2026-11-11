package az.zovrax.inventorypickup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Independent, client-local configuration. Never modifies player inventories or server state.
 */
public final class PickupConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("zovrax-inventory-pickup.json");

    public boolean enabled = true;
    public boolean showTotal = true;
    public boolean showGain = true;
    public boolean allowOverScreens = false;
    public boolean mergeRepeated = true;
    public int durationMs = 2500;
    public int animationMs = 350;
    public int margin = 12;
    public int maxVisible = 4;
    public int backgroundOpacity = 80; // percent (0..100)
    public int position = 0; // 0 top-right, 1 top-left, 2 bottom-right, 3 bottom-left

    public static PickupConfig load() {
        try {
            if (Files.exists(FILE)) {
                PickupConfig config = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), PickupConfig.class);
                if (config != null) {
                    config.sanitize();
                    return config;
                }
            }
        } catch (Exception ignored) {
            // Corrupt/unknown JSON should never stop Minecraft from launching.
        }
        return new PickupConfig();
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this) + "\n", StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.err.println("[Zovrax Inventory Pickup] Could not save settings: " + exception.getMessage());
        }
    }

    private void sanitize() {
        durationMs = clamp(durationMs, 800, 10000);
        animationMs = clamp(animationMs, 100, 900);
        margin = clamp(margin, 4, 80);
        maxVisible = clamp(maxVisible, 1, 6);
        backgroundOpacity = clamp(backgroundOpacity, 0, 100);
        position = clamp(position, 0, 3);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

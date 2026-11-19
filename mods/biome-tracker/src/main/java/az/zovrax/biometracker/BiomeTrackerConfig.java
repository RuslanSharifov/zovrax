package az.zovrax.biometracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BiomeTrackerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-biome-tracker.json");

    public boolean hudEnabled = true;
    public boolean notifications = true;
    public boolean notificationSound = true;
    public boolean showBoundaries = false;
    public boolean showCurrentBiomeTime = true;
    public int scale = 100;
    public int opacity = 100;
    public int backgroundOpacity = 35;
    public double x = 0.5;
    public double y = 0.04;

    public static BiomeTrackerConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                BiomeTrackerConfig config = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), BiomeTrackerConfig.class);
                if (config != null) { config.sanitize(); return config; }
            }
        } catch (Exception e) {
            System.err.println("[Biome Tracker] Could not load config: " + e.getMessage());
        }
        return new BiomeTrackerConfig();
    }

    public void sanitize() {
        scale = Math.clamp(scale, 60, 160);
        opacity = Math.clamp(opacity, 0, 100);
        backgroundOpacity = Math.clamp(backgroundOpacity, 0, 100);
        if (!Double.isFinite(x)) x = 0.5;
        if (!Double.isFinite(y)) y = 0.04;
        x = Math.clamp(x, 0, 1);
        y = Math.clamp(y, 0, 1);
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this) + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[Biome Tracker] Could not save config: " + e.getMessage());
        }
    }
}

package az.zovrax.farsight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistent, independent FarSight client configuration. */
public final class FarSightConfig {
    public static final String MOD_ID = "zovrax_farsight";
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-farsight.json");

    public boolean adaptiveEnabled = false;
    public int minRenderDistance = 6;
    public int maxRenderDistance = 32;
    public int targetFps = 60;
    public int heapPressurePercent = 82;
    public int sampleIntervalTicks = 100;
    public int fpsDropTolerance = 8;
    public int fpsRecoveryHeadroom = 15;
    public int heapRecoveryHeadroom = 12;

    public static FarSightConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                FarSightConfig loaded = JSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), FarSightConfig.class);
                if (loaded != null) { loaded.sanitize(); return loaded; }
            }
        } catch (Exception exception) {
            System.err.println("[FarSight] Could not read config; using defaults: " + exception.getMessage());
        }
        return new FarSightConfig();
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, JSON.toJson(this) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            System.err.println("[FarSight] Could not save config: " + exception.getMessage());
        }
    }

    private void sanitize() {
        minRenderDistance = clamp(minRenderDistance, 2, 32);
        maxRenderDistance = clamp(maxRenderDistance, minRenderDistance, 32);
        targetFps = clamp(targetFps, 20, 360);
        heapPressurePercent = clamp(heapPressurePercent, 50, 98);
        sampleIntervalTicks = clamp(sampleIntervalTicks, 20, 1200);
        fpsDropTolerance = clamp(fpsDropTolerance, 1, 60);
        fpsRecoveryHeadroom = clamp(fpsRecoveryHeadroom, 1, 120);
        heapRecoveryHeadroom = clamp(heapRecoveryHeadroom, 1, 40);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

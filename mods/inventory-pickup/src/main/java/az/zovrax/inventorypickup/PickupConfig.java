package az.zovrax.inventorypickup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistent client-only options. Old corner/margin settings migrate on first use. */
public final class PickupConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-inventory-pickup.json");

    public boolean enabled = true;
    public boolean showTotal = true;
    public boolean showGain = true;
    public boolean allowOverScreens = false;
    public boolean mergeRepeated = true;
    public int durationMs = 2500;
    public int animationMs = 350;
    public int margin = 12;
    public int maxVisible = 4;
    public int backgroundOpacity = 80;
    public int position = 0;
    // -1 = migrate legacy corner on first render; otherwise normalized top-left [0..1].
    public double hudX = -1;
    public double hudY = -1;
    public int notificationScale = 100; // 50%..200%, step 5%

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
            // Corrupt JSON should not prevent startup.
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

    public void ensurePosition(int screenWidth, int screenHeight) {
        if (hudX >= 0 && hudY >= 0) return;
        int w = Math.max(1, screenWidth - Math.round(194f * notificationScale / 100f));
        int h = Math.max(1, screenHeight - Math.round(32f * notificationScale / 100f));
        hudX = ((position & 1) == 0) ? (double) Math.max(0, w - margin) / w : (double) margin / w;
        hudY = (position < 2) ? (double) margin / h : (double) Math.max(0, h - margin) / h;
        hudX = clampDouble(hudX, 0, 1);
        hudY = clampDouble(hudY, 0, 1);
        save();
    }

    private void sanitize() {
        durationMs = clamp(durationMs, 800, 10000);
        animationMs = clamp(animationMs, 100, 900);
        margin = clamp(margin, 0, 80);
        maxVisible = clamp(maxVisible, 1, 6);
        backgroundOpacity = clamp(backgroundOpacity, 0, 100);
        position = clamp(position, 0, 3);
        notificationScale = clamp(notificationScale, 50, 200);
        if (!Double.isFinite(hudX)) hudX = -1;
        if (!Double.isFinite(hudY)) hudY = -1;
        if (hudX >= 0) hudX = clampDouble(hudX, 0, 1);
        if (hudY >= 0) hudY = clampDouble(hudY, 0, 1);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

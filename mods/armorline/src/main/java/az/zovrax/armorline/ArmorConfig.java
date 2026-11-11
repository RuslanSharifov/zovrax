package az.zovrax.armorline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Independent, local-only ArmorLine settings. Never shares Inventory Pickup's config. */
public final class ArmorConfig {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-armorline.json");
    public boolean enabled = true;
    public boolean showDurabilityBar = true;
    public boolean showDurabilityNumbers = false;
    public boolean vertical = true;
    public int scale = 100;     // 50..200%, 5% steps
    public int opacity = 100;   // 0..100%, 1% steps
    public double x = -1;       // normalized top-left in available screen area
    public double y = -1;

    public static ArmorConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                ArmorConfig config = JSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), ArmorConfig.class);
                if (config != null) { config.sanitize(); return config; }
            }
        } catch (Exception e) {
            System.err.println("[ArmorLine] Ignoring invalid config: " + e.getMessage());
        }
        return new ArmorConfig();
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, JSON.toJson(this) + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[ArmorLine] Cannot save settings: " + e.getMessage());
        }
    }

    public void ensurePosition(int width, int height) {
        if (x >= 0 && y >= 0) return;
        x = 1.0;
        y = 0.28;
        save();
    }

    public void sanitize() {
        scale = Math.clamp(scale, 50, 200);
        opacity = Math.clamp(opacity, 0, 100);
        if (!Double.isFinite(x)) x = -1;
        if (!Double.isFinite(y)) y = -1;
        if (x >= 0) x = Math.clamp(x, 0, 1);
        if (y >= 0) y = Math.clamp(y, 0, 1);
    }
}

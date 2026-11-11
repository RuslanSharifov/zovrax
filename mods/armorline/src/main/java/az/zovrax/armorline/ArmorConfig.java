package az.zovrax.armorline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Independent client-only ArmorLine config, with migration of 0.1.0 options. */
public final class ArmorConfig {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-armorline.json");

    // Durability: 0 = bars + numbers, 1 = bars, 2 = numbers, 3 = hidden.
    // Indicator side: 0 = auto (inside screen), 1 = right, 2 = left, 3 = hidden.
    public boolean enabled = true;
    public boolean vertical = true;
    public int durabilityMode = 2;
    public int indicatorSide = 0;
    public int scale = 100;
    public int opacity = 100;           // overall HUD alpha
    public int backgroundOpacity = 0; // independent 0-100% panel alpha
    public int iconsOpacity = 100;      // icon/slot layer alpha
    public int textOpacity = 100;       // numerical labels layer alpha
    public boolean showMainHand = true;
    public boolean showOffHand = true;
    public double x = -1;
    public double y = -1;

    // Kept for reading the original 0.1.0 config without losing player settings.
    @Deprecated public boolean showDurabilityBar = true;
    @Deprecated public boolean showDurabilityNumbers = false;

    public static ArmorConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                String source = Files.readString(FILE, StandardCharsets.UTF_8);
                ArmorConfig config = JSON.fromJson(source, ArmorConfig.class);
                if (config != null) {
                    if (!source.contains("\"durabilityMode\"")) {
                        config.durabilityMode = config.showDurabilityBar
                            ? (config.showDurabilityNumbers ? 0 : 1)
                            : (config.showDurabilityNumbers ? 2 : 3);
                    }
                    config.sanitize();
                    return config;
                }
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
        backgroundOpacity = Math.clamp(backgroundOpacity, 0, 100);
        iconsOpacity = Math.clamp(iconsOpacity, 0, 100);
        textOpacity = Math.clamp(textOpacity, 0, 100);
        durabilityMode = Math.clamp(durabilityMode, 0, 3);
        indicatorSide = Math.clamp(indicatorSide, 0, 3);
        if (!Double.isFinite(x)) x = -1;
        if (!Double.isFinite(y)) y = -1;
        if (x >= 0) x = Math.clamp(x, 0, 1);
        if (y >= 0) y = Math.clamp(y, 0, 1);
    }
}

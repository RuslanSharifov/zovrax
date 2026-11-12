package az.zovrax.chromaoutline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Independent client-only configuration saved on every settings change. */
public final class ChromaOutlineConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-chroma-outline.json");

    public boolean enabled = true;
    public String mode = "RAINBOW"; // SOLID, RAINBOW
    public int red = 80;
    public int green = 220;
    public int blue = 255;
    public int opacity = 100;
    public int rainbowSpeed = 20;       // 1..100
    public int rainbowSaturation = 100; // percent
    public int rainbowBrightness = 100; // percent
    public boolean pulseEnabled = false;
    public int pulseSpeed = 20;         // 1..100
    public int pulseDepth = 45;         // 0..90 percent brightness variation
    public int outlineWidth = 1;       // 1..5; additional offset passes, not pixel width
    public int expansion = 0;          // 0..20 milliblocks, whole shape bounding-box expansion
    public boolean fullCube = false;

    public static ChromaOutlineConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                ChromaOutlineConfig value = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), ChromaOutlineConfig.class);
                if (value != null) { value.sanitize(); return value; }
            }
        } catch (Exception e) {
            System.err.println("[Chroma Outline] Could not read settings: " + e.getMessage());
        }
        return new ChromaOutlineConfig();
    }

    public void sanitize() {
        if (!"SOLID".equals(mode) && !"RAINBOW".equals(mode)) mode = "RAINBOW";
        red = Math.clamp(red, 0, 255);
        green = Math.clamp(green, 0, 255);
        blue = Math.clamp(blue, 0, 255);
        opacity = Math.clamp(opacity, 0, 100);
        rainbowSpeed = Math.clamp(rainbowSpeed, 1, 100);
        rainbowSaturation = Math.clamp(rainbowSaturation, 0, 100);
        rainbowBrightness = Math.clamp(rainbowBrightness, 1, 100);
        pulseSpeed = Math.clamp(pulseSpeed, 1, 100);
        pulseDepth = Math.clamp(pulseDepth, 0, 90);
        outlineWidth = Math.clamp(outlineWidth, 1, 5);
        expansion = Math.clamp(expansion, 0, 20);
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this) + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[Chroma Outline] Could not save settings: " + e.getMessage());
        }
    }
}

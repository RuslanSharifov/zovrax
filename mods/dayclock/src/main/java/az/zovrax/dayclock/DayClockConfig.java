package az.zovrax.dayclock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Independent, client-only config; values are saved immediately. */
public final class DayClockConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zovrax-dayclock.json");

    public boolean enabled = true;
    public boolean showDay = true;
    public boolean showTime = true;
    public boolean showCoordinates = false;
    public boolean showBiome = false;
    public boolean showDimension = false;
    public boolean showFacing = false;
    public boolean showWeather = false;
    public boolean showLight = false;
    public int scale = 100;
    public int opacity = 100;
    public int backgroundOpacity = 25;
    public double x = 0.98;
    public double y = 0.055;

    public static DayClockConfig load() {
        try {
            if (Files.isRegularFile(FILE)) {
                DayClockConfig value = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), DayClockConfig.class);
                if (value != null) { value.sanitize(); return value; }
            }
        } catch (Exception e) {
            System.err.println("[DayClock] Failed to read config: " + e.getMessage());
        }
        return new DayClockConfig();
    }

    public void sanitize() {
        scale = Math.clamp(scale, 50, 200);
        opacity = Math.clamp(opacity, 0, 100);
        backgroundOpacity = Math.clamp(backgroundOpacity, 0, 100);
        if (!Double.isFinite(x)) x = 0.98;
        if (!Double.isFinite(y)) y = 0.055;
        x = Math.clamp(x, 0d, 1d);
        y = Math.clamp(y, 0d, 1d);
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this) + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[DayClock] Failed to save config: " + e.getMessage());
        }
    }
}
